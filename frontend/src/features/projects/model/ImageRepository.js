/**
 * @typedef {{ id: string, name: string, url: string }} ProjectImage
 * @typedef {{ upload: (file: File) => Promise<ProjectImage> }} ImageRepository
 */
export class ApiImageRepository {
  /** @param {File} file @returns {Promise<ProjectImage>} */
  async upload() {
    throw new Error("Não foi possível enviar a imagem no momento.")
  }
}
/** @type {ImageRepository} */
export const imageRepository = new ApiImageRepository()
