package com.yf.document.modules.news.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yf.boot.base.api.api.ApiRest;
import com.yf.boot.base.api.api.controller.BaseController;
import com.yf.boot.base.api.api.dto.BaseIdReqDTO;
import com.yf.boot.base.api.api.dto.BaseIdRespDTO;
import com.yf.boot.base.api.api.dto.BaseIdsReqDTO;
import com.yf.boot.base.api.api.dto.PagingReqDTO;
import com.yf.boot.base.api.utils.BeanMapper;
import com.yf.document.modules.news.dto.TNewsDTO;
import com.yf.document.modules.news.entity.TNews;
import com.yf.document.modules.news.service.TNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
* <p>
* 新闻列表控制器
* </p>
*
* @author Panjp
* @since 2021-09-22 14:51
*/
@Api(tags={"新闻列表"})
@RestController
@RequestMapping("/api/news/news/")
public class TNewsController extends BaseController {

    @Autowired
    private TNewsService baseService;

    /**
    * 添加或修改
    * @param reqDTO
    * @return
    */
    @ApiOperation(value = "添加或修改")
    @RequestMapping(value = "/save", method = { RequestMethod.POST})
    public ApiRest save(@RequestBody TNewsDTO reqDTO) {
        baseService.save(reqDTO);
        return super.success();
    }

    /**
     * 发布新闻
     * @param reqDTO
     * @return
     */
    @ApiOperation(value = "发布新闻")
    @RequestMapping(value = "/fbNews", method = { RequestMethod.POST})
    public ApiRest fbNews(@RequestBody TNewsDTO reqDTO) {
        reqDTO = baseService.detail(reqDTO.getId());
        reqDTO.setNewsStatus("1");
        baseService.save(reqDTO);
        return super.success();
    }

    /**
     * 取消发布新闻
     * @param reqDTO
     * @return
     */
    @ApiOperation(value = "取消发布新闻")
    @RequestMapping(value = "/qxFbNews", method = { RequestMethod.POST})
    public ApiRest qxFbNews(@RequestBody TNewsDTO reqDTO) {
        reqDTO = baseService.detail(reqDTO.getId());
        reqDTO.setNewsStatus("0");
        baseService.save(reqDTO);
        return super.success();
    }

    /**
    * 批量删除
    * @param reqDTO
    * @return
    */
    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/delete", method = { RequestMethod.POST})
    public ApiRest delete(@RequestBody BaseIdsReqDTO reqDTO) {
        //根据ID删除
        baseService.delete(reqDTO.getIds());
        return super.success();
    }

    /**
    * 查找详情
    * @param reqDTO
    * @return
    */
    @ApiOperation(value = "查找详情")
    @RequestMapping(value = "/detail", method = { RequestMethod.POST})
    public ApiRest<TNewsDTO> detail(@RequestBody BaseIdReqDTO reqDTO) {
        TNewsDTO dto = baseService.detail(reqDTO.getId());
        return super.success(dto);
    }

    /**
    * 分页查找
    * @param reqDTO
    * @return
    */
    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/paging", method = { RequestMethod.POST})
    public ApiRest<IPage<TNewsDTO>> paging(@RequestBody PagingReqDTO<TNewsDTO> reqDTO) {

        //分页查询并转换
        IPage<TNewsDTO> page = baseService.paging(reqDTO);

        return super.success(page);
    }

    /**
     * 分页查找
     * @param reqDTO
     * @return
     */
    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/webPaging", method = { RequestMethod.POST})
    public ApiRest<IPage<TNewsDTO>> webPaging(@RequestBody PagingReqDTO<TNewsDTO> reqDTO) {
        TNewsDTO dto = reqDTO.getParams();
        if(null == dto){
            dto = new TNewsDTO();
        }
        dto.setNewsStatus("1");
        //分页查询并转换
        IPage<TNewsDTO> page = baseService.paging(reqDTO);

        return super.success(page);
    }

    /**
     * 查找列表，每次最多返回200条数据
     * @param reqDTO
     * @return
     */
    @ApiOperation(value = "查找列表")
    @RequestMapping(value = "/list", method = { RequestMethod.POST})
    public ApiRest<List<TNewsDTO>> list(@RequestBody TNewsDTO reqDTO) {
        // 查找列表
        List<TNewsDTO> dtoList = baseService.list(reqDTO);
        return super.success(dtoList);
    }


}
