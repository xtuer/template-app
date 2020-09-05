package com.xtuer.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 机构
 */
@Getter
@Setter
@Accessors(chain = true)
public class Organization {
    private long orgId; // 机构 ID

    @NotBlank(message="机构名称 不能为空!")
    private String name;     // 机构名字

    private String host;     // 机构域名
    private int    port;     // 网站端口
    private long   adminId;  // 管理员 ID
    private long   parentId; // 上级机构 ID

    @NotBlank(message="单位对接人名字 不能为空!")
    private String contactPerson; // 单位对接人名字

    @NotBlank(message="单位对接人电话 不能为空!")
    private String contactMobile; // 单位对接人电话

    @NotBlank(message="门户平台名称 不能为空!")
    private String portalName;    // 门户平台名称

    @NotBlank(message="机构 Logo 不能为空!")
    private String logo;          // Logo

    private boolean enabled = true; // 是否启用

    User admin; // 管理员
}
