/**
 * @typedef {{ id: string, name: string, url: string }} ProjectImage
 * @typedef {{ upload: (file: File) => Promise<ProjectImage> }} ImageRepository
 */
export class MockImageRepository {
  /** @param {File} file @returns {Promise<ProjectImage>} */
  async upload(file) {
    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      throw new Error("Selecione uma imagem JPG, PNG ou WebP.")
    }
    if (file.size > 1024 * 1024) {
      throw new Error(
        "No modo demonstrativo, selecione uma imagem de até 1 MB.",
      )
    }
    const url = await new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(String(reader.result))
      reader.onerror = () => reject(new Error("Não foi possível ler a imagem."))
      reader.readAsDataURL(file)
    })
    return { id: crypto.randomUUID(), name: file.name, url }
  }
}
// Composition boundary: replace only this adapter to connect the future API.
/** @type {ImageRepository} */
export const imageRepository = new MockImageRepository()
