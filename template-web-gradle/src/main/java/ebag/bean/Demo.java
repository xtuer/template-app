package ebag.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Accessors(chain=true) // 可以链式调用 setter
public class Demo {
    @NotNull(message="ID 不能为 null")
    @Min(value=1, message="ID 不能小于 1")
    private Long id;

    @NotBlank(message="Info 不能为空")
    private String info;

    private boolean marked;

    public Demo() {
    }

    public Demo(Long id, String info) {
        this.id = id;
        this.info = info;
    }

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.setId(1L).setInfo("Hello");
        System.out.printf("ID: %d, Info: %s\n", demo.getId(), demo.getInfo());
        System.out.println(demo);
    }
}
