package com.teste.apis.imagem;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Imagem enviada ao Cloudinary. Entity nao aparece em assinatura de controller. */
@Getter
@Setter
@Entity
@Table(name = "imagens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Imagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String publicId;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false, length = 20)
    private String formato;

    @Column(nullable = false)
    private Long bytes;

    private Integer largura;

    private Integer altura;

    @Column(nullable = false)
    private OffsetDateTime criadoEm;

    public static Imagem de(String publicId, String url, String formato, Long bytes,
                            Integer largura, Integer altura, OffsetDateTime criadoEm) {
        Imagem i = new Imagem();
        i.publicId = publicId;
        i.url = url;
        i.formato = formato;
        i.bytes = bytes;
        i.largura = largura;
        i.altura = altura;
        i.criadoEm = criadoEm;
        return i;
    }
}
