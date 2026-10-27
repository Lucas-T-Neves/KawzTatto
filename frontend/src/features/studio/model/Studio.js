/**
 * @typedef {{ id: string, name: string, [key: string]: string }} Entry
 * @typedef {Record<string, Entry[]>} Store
 *
 * @typedef {object} StudioRepository
 * @property {() => Store} load
 * @property {(data: Store) => void} save
 *
 * Contracts documented with JSDoc: the application runs as plain JavaScript.
 */
export {}
