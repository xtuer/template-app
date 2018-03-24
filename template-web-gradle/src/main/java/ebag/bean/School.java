package ebag.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.LinkedList;
import java.util.List;

/**
 * 学校类
 */
@Getter
@Setter
@Accessors(chain = true)
public class School {
    private Long    id;
    private Long    adminId;          // 管理员帐号的 ID

    @NotBlank(message="全称不能为空(name)")
    private String  name;             // 全称

    private String  abbreviationName; // 简称

    @NotBlank(message="域名不能为空(host)")
    private String  host;             // 站点域名

    private Integer foundingYear;     // 建校时间

    private String  contactPerson;    // 联系人
    private String  contactPhone;     // 联系电话
    private String  contactEmail;     // 联系邮件

    private Address address;          // 学校地址
    private boolean enabled;          // 是否可用
    private boolean messageEnabled;   // 是否开启短信登录
    private String  messageTemplate;  // 短信模板
    private List<String> educationTypes = new LinkedList<>(); // 教育类型
}
