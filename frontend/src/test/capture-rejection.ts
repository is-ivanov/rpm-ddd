export function captureRejection(promise: Promise<unknown>): Promise<unknown> {
  return promise.then(
    () => {
      throw new Error('call resolved but should have rejected on an error status');
    },
    (error: unknown) => error,
  );
}
