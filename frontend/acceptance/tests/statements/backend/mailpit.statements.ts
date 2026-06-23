import { type APIRequestContext, expect, request } from '@playwright/test';
import { z } from 'zod';

const MAILPIT_API_URL = process.env.MAILPIT_API_URL || 'http://localhost:8025';

const ACTIVATION_SUBJECT = 'Activate your RPM account';
const ACTIVATION_LINK_PATTERN = /https?:\/\/[^\s"<>]+\/activate\?token=[A-Za-z0-9._-]+/;
const ACTIVATION_PATH = '/activate';
const TOKEN_QUERY_PARAM = 'token';

const mailpitSearchResultSchema = z.object({
  messages: z.array(z.object({ ID: z.string(), Subject: z.string() })),
});

const mailpitMessageSchema = z.object({
  Subject: z.string(),
  Text: z.string(),
});

// Reads the activation email the real backend delivers to Mailpit and extracts
// the activation token from the link. The link points at the local frontend
// (rpm.frontend-base-url -> http://localhost:5173), so the journey opens the
// activation page as the external entry point the user genuinely arrives at.
export class MailpitStatements {
  async readActivationTokenFor(email: string): Promise<string> {
    const api = await request.newContext({ baseURL: MAILPIT_API_URL });
    try {
      const messageId = await this.waitForActivationMessage(api, email);
      return await this.extractActivationToken(api, messageId);
    } finally {
      await api.dispose();
    }
  }

  // Polling wait (never sleep): the activation email is delivered asynchronously
  // after the create-user call, so poll the Mailpit search API until it appears.
  private async waitForActivationMessage(api: APIRequestContext, email: string): Promise<string> {
    let messageId = '';
    await expect
      .poll(async () => (messageId = await this.findActivationMessageId(api, email)), {
        message: `activation email is delivered to Mailpit for ${email}`,
        timeout: 15_000,
      })
      .not.toBe('');
    return messageId;
  }

  private async findActivationMessageId(api: APIRequestContext, email: string): Promise<string> {
    const query = encodeURIComponent(`to:${email}`);
    const response = await api.get(`/api/v1/search?query=${query}`);
    if (response.status() !== 200) {
      return '';
    }
    const result = mailpitSearchResultSchema.parse(await response.json());
    const message = result.messages.find((candidate) => candidate.Subject === ACTIVATION_SUBJECT);
    return message ? message.ID : '';
  }

  private async extractActivationToken(api: APIRequestContext, messageId: string): Promise<string> {
    const response = await api.get(`/api/v1/message/${messageId}`);
    expect(response.status(), 'Mailpit returns the activation email body').toBe(200);
    const message = mailpitMessageSchema.parse(await response.json());
    expect(message.Subject, 'the delivered email is the activation email').toBe(ACTIVATION_SUBJECT);
    const match = ACTIVATION_LINK_PATTERN.exec(message.Text);
    expect(match, 'activation email body contains an absolute frontend activation link').not.toBeNull();
    // Parse the matched link as a real URL (no string-splitting): the host is
    // environment-overridable (RPM_FRONTEND_BASE_URL), so we pin the deterministic
    // shape — exact /activate path carrying a non-empty token query param.
    const activationLink = new URL(match![0]);
    expect(activationLink.pathname, 'activation link points at the exact /activate page').toBe(ACTIVATION_PATH);
    const token = activationLink.searchParams.get(TOKEN_QUERY_PARAM);
    expect(token, 'activation link carries a non-empty token query param').toBeTruthy();
    return token!;
  }
}
