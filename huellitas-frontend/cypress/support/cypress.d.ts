declare namespace Cypress {
  interface Chainable {
    session(id: string | string[], setup: () => void): Chainable<void>
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    env(key: string): any
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    env(key?: string): Record<string, any>
  }
}
