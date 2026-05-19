package com.ydh.aicodegenrate.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeleteRequest implements Serializable {
    /**
     * id app id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
