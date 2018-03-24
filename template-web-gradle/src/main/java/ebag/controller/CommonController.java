package ebag.controller;

import ebag.bean.Region;
import ebag.bean.Result;
import ebag.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommonController {
    @Autowired
    private AddressMapper addressMapper;

    /**
     * 查找 parentId 为传入的 parentId 的地区
     * URL: http://localhost:8080/api/regions?parentId=10
     * 参数: parentId
     *
     * @param parentId 地区的 parentId
     */
    @GetMapping(Urls.API_REGIONS)
    @ResponseBody
    public Result<Region> findRegions(@RequestParam Integer parentId) {
        return Result.ok(addressMapper.findRegionsByParentId(parentId));
    }
}
