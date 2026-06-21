export interface ComplexityRule {
  readonly key: string;
  readonly label: string;
  readonly met: boolean;
}

export function evaluateComplexityRules(_password: string): ComplexityRule[] {
  throw new Error('not implemented');
}
