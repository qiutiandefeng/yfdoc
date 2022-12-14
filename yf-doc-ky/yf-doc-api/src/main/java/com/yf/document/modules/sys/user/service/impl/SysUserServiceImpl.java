package com.yf.document.modules.sys.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.boot.base.api.api.ApiError;
import com.yf.boot.base.api.api.dto.PagingReqDTO;
import com.yf.boot.base.api.api.enums.CommonState;
import com.yf.boot.base.api.exception.ServiceException;
import com.yf.boot.base.api.utils.BeanMapper;
import com.yf.boot.base.api.utils.StringUtils;
import com.yf.boot.base.api.utils.passwd.PassHandler;
import com.yf.boot.base.api.utils.passwd.PassInfo;
import com.yf.document.ability.Constant;
import com.yf.document.ability.captcha.service.CaptchaService;
import com.yf.document.ability.redis.service.RedisService;
import com.yf.document.ability.shiro.jwt.JwtUtils;
import com.yf.document.config.BaseConfig;
import com.yf.document.modules.sys.depart.service.SysDepartService;
import com.yf.document.modules.sys.log.service.SysLogActiveService;
import com.yf.document.modules.sys.user.dto.SysUserDTO;
import com.yf.document.modules.sys.user.dto.request.*;
import com.yf.document.modules.sys.user.dto.response.SysUserLoginDTO;
import com.yf.document.modules.sys.user.dto.response.UserExportDTO;
import com.yf.document.modules.sys.user.dto.response.UserListRespDTO;
import com.yf.document.modules.sys.user.entity.SysRole;
import com.yf.document.modules.sys.user.entity.SysUser;
import com.yf.document.modules.sys.user.entity.SysUserBind;
import com.yf.document.modules.sys.user.mapper.SysUserMapper;
import com.yf.document.modules.sys.user.service.SysUserBindService;
import com.yf.document.modules.sys.user.service.SysUserRoleService;
import com.yf.document.modules.sys.user.service.SysUserService;
import com.yf.document.modules.sys.user.utils.SignUtils;
import com.yf.document.modules.user.UserUtils;
import com.yf.document.utils.CacheKey;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* <p>
* ???????????? ???????????????
* </p>
*
* @author ????????????
* @since 2020-04-13 16:57
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private BaseConfig baseConfig;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CaptchaService captchaService;


    @Autowired
    private SysLogActiveService sysLogActiveService;

    @Autowired
    private SysUserBindService sysUserBindService;

    @Autowired
    private SysDepartService sysDepartService;


    private static final String ROLE_STUDENT = "sa";
    private static final String DEFAULT_PASS = "123456";


    @Override
    public IPage<UserListRespDTO> paging(PagingReqDTO<SysUserQueryReqDTO> reqDTO) {

        //??????????????????
        Page page = reqDTO.toPage();

        //????????????
        IPage<UserListRespDTO> pageData = baseMapper.paging(page, reqDTO.getParams());
        return pageData;
     }

    @Override
    public SysUserLoginDTO login(SysUserLoginReqDTO reqDTO) {

        // ?????????????????????
        if(!StringUtils.isBlank(reqDTO.getCaptchaKey())) {
            boolean check = captchaService.checkCaptcha(reqDTO.getCaptchaKey(), reqDTO.getCaptchaValue());
            if (!check) {
                throw new ServiceException("???????????????????????????????????????");
            }
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUsername());
        SysUser user = this.getOne(wrapper, false);

        // ??????????????????&??????
        return this.checkAndLogin(user,  reqDTO.getPassword());
    }

    @Override
    public SysUserLoginDTO mobileLogin(MobileLoginReqDTO reqDTO) {

        // ????????????
        boolean check = false;

        if(!check){
            throw new ServiceException("????????????????????????????????????");
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getMobile, reqDTO.getMobile());
        SysUser user = this.getOne(wrapper, false);

        // ??????????????????
        return this.checkAndLogin(user, null);
    }


    /**
     * ??????????????????
     * @param user
     */
    private SysUserLoginDTO checkAndLogin(SysUser user, String password){

        if(user == null){
            throw new ServiceException(ApiError.ERROR_90010001);
        }

        // ?????????
        if(user.getState().equals(CommonState.ABNORMAL)){
            throw new ServiceException(ApiError.ERROR_90010005);
        }

        if(!StringUtils.isBlank(password)){
            boolean pass = PassHandler.checkPass(password , user.getSalt(), user.getPassword());
            if(!pass){
                throw new ServiceException(ApiError.ERROR_90010002);
            }
        }

        return this.setToken(user);
    }

    @Cacheable(value = CacheKey.TOKEN, key = "#token")
    @Override
    public SysUserLoginDTO token(String token) {

        // ????????????
        String username;
        try {
            username = JwtUtils.getUsername(token);
        } catch (Exception e) {
            throw new ServiceException("?????????????????????????????????");
        }

        JSONObject json = redisService.getJson(Constant.USER_NAME_KEY+username);
        if(json == null){
            throw new ServiceException(ApiError.ERROR_10010002);
        }


        SysUserLoginDTO respDTO = json.toJavaObject(SysUserLoginDTO.class);

        // T???????????????
        if(baseConfig.getLoginTick()){
            if(!token.equals(respDTO.getToken())){
                throw new ServiceException("???????????????????????????????????????");
            }
        }

        // ??????????????????
        SysUser user = this.getById(respDTO.getId());

        if(user == null){
            // ???????????????????????????
            throw new ServiceException(ApiError.ERROR_10010002);
        }

        // ????????????
        sysLogActiveService.merge(respDTO.getId());
        respDTO.setPoints(user.getPoints());
        return respDTO;
    }

    @CacheEvict(value = CacheKey.TOKEN, key = "#token")
    @Override
    public void logout(String token) {

        if(baseConfig.getLoginTick()){
            try {
                String username = JwtUtils.getUsername(token);
                String [] keys = new String[]{Constant.USER_NAME_KEY+username};
                redisService.del(keys);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(SysUserDTO reqDTO) {


        SysUser user = this.getById(UserUtils.getUserId());

        // ????????????
        String pass = reqDTO.getPassword();
        if(!StringUtils.isBlank(pass)){
            PassInfo passInfo = PassHandler.buildPassword(pass);
            user.setPassword(passInfo.getPassword());
            user.setSalt(passInfo.getSalt());
            this.updateById(user);

            // ????????????
            String [] keys = new String[]{Constant.USER_NAME_KEY+user.getUserName()};
            redisService.del(keys);
        }

        String avatar = reqDTO.getAvatar();
        if(!StringUtils.isBlank(avatar)) {
            user.setAvatar(avatar);
            this.updateById(user);
            this.setToken(user);
        }


    }

    @Override
    public void pass(SysUserPassReqDTO reqDTO) {

        // ??????????????????
        SysUser user = this.getById(UserUtils.getUserId());

        boolean check = PassHandler.checkPass(reqDTO.getOldPassword(), user.getSalt(), user.getPassword());
        if(!check){
            throw new ServiceException(ApiError.ERROR_90010007);
        }

        PassInfo passInfo = PassHandler.buildPassword(reqDTO.getPassword());
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());
        this.updateById(user);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(SysUserSaveReqDTO reqDTO) {

        List<String> roles = reqDTO.getRoles();

        if(CollectionUtils.isEmpty(roles)){
            throw new ServiceException(ApiError.ERROR_90010003);
        }


        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUserName());
        if(!StringUtils.isBlank(reqDTO.getId())){
            wrapper.lambda().ne(SysUser::getId, reqDTO.getId());
        }

        int count = this.count(wrapper);
        if(count > 0){
            throw new ServiceException("????????????????????????");
        }


        // ??????????????????
        SysUser user = new SysUser();
        BeanMapper.copy(reqDTO, user);

        // ????????????
        if(StringUtils.isBlank(user.getId())){
            user.setId(IdWorker.getIdStr());
        }

        // ????????????
        if(!StringUtils.isBlank(reqDTO.getPassword())){
            PassInfo pass = PassHandler.buildPassword(reqDTO.getPassword());
            user.setPassword(pass.getPassword());
            user.setSalt(pass.getSalt());
        }

        // ??????????????????
        sysUserRoleService.saveRoles(user.getId(), roles);
        this.saveOrUpdate(user);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysUserLoginDTO reg(MobileRegReqDTO reqDTO) {


        return this.saveAndLogin(reqDTO.getMobile(),
                reqDTO.getDeptCode(),
                reqDTO.getRealName(),
                null,
                reqDTO.getMobile(),
                "",
                reqDTO.getPassword());
    }


    /**
     * ???????????????????????????
     * @param userName
     * @param deptCode
     * @param realName
     * @param mobile
     * @param avatar
     * @param password
     * @return
     */
    private SysUserLoginDTO saveAndLogin(String userName, String deptCode, String realName, String role, String mobile, String avatar, String password){

        // ????????????
        SysUser user = new SysUser();
        user.setId(IdWorker.getIdStr());
        user.setUserName(userName);
        user.setRealName(realName);
        user.setDeptCode(deptCode);
        user.setMobile(mobile);
        user.setAvatar(avatar);
        PassInfo passInfo = PassHandler.buildPassword(password);
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());

        // ????????????
        List<String> roleList = new ArrayList<>();
        if(!StringUtils.isBlank(role)){
            roleList.add(role);
        }else{
            // ????????????
            roleList.add(ROLE_STUDENT);
        }


        // ????????????
        sysUserRoleService.saveRoles(user.getId(), roleList);

        // ????????????
        this.save(user);
        return this.setToken(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(List<UserExportDTO> reqDTO) {
        for(UserExportDTO item: reqDTO){
            this.importUser(item);
        }
    }

    @Override
    public List<UserExportDTO> listForExport(SysUserQueryReqDTO reqDTO) {
        return baseMapper.listForExport(reqDTO);
    }

    @Override
    public SysUserSaveReqDTO detail(String id) {

        // ??????????????????
        SysUser user = this.getById(id);
        SysUserSaveReqDTO respDTO = new SysUserSaveReqDTO();
        BeanMapper.copy(user, respDTO);

        // ????????????
        List<String> roles = sysUserRoleService.listRoleIds(user.getId());
        respDTO.setRoles(roles);
        return respDTO;
    }

    @Override
    public List<String> listByDept(List<String> codes) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(SysUser::getId)
                .in(SysUser::getDeptCode, codes);

        List<SysUser> list = this.list(wrapper);
        List<String> ids = new ArrayList<>();
        if(!CollectionUtils.isEmpty(list)){
            for(SysUser user: list){
                ids.add(user.getId());
            }
        }
        return ids;
    }

    @Override
    public void batchDept(UserDeptReqDTO reqDTO) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(SysUser::getId, reqDTO.getUserIds());

        SysUser user = new SysUser();
        user.setDeptCode(reqDTO.getDeptCode());
        this.update(user, wrapper);
    }

    @Override
    public void resetPass(ResetPassReqDTO reqDTO) {

        // ????????????
        boolean check = false;

        if(!check){
            throw new ServiceException("????????????????????????????????????");
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getMobile, reqDTO.getMobile());
        SysUser user = this.getOne(wrapper, false);

        if(user == null){
            throw new ServiceException("???????????????????????????????????????");
        }

        PassInfo passInfo = PassHandler.buildPassword(reqDTO.getNewPassword());
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());
        this.updateById(user);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysUserLoginDTO loginByThird(String loginType, String openId, String nickName, String avatar) {

        String userId = sysUserBindService.findBind(loginType, openId);

        // ??????????????????????????????
        if(StringUtils.isBlank(userId)){

            // ??????????????????
            SysUserLoginDTO dto = this.saveAndLogin(RandomStringUtils.randomAlphabetic(16),
                    "A01",
                    nickName,
                    null,
                    RandomStringUtils.randomAlphabetic(16),
                    avatar,
                    RandomStringUtils.randomAlphanumeric(32));

            // ??????
            SysUserBind bind = new SysUserBind();
            bind.setLoginType(loginType);
            bind.setUserId(dto.getId());
            bind.setOpenId(openId);
            sysUserBindService.save(bind);

            return dto;
        }


        // ??????????????????&??????
        SysUser user = this.getById(userId);
        return this.checkAndLogin(user, null);
    }


    /**
     * ????????????
     * @param reqDTO
     */
    private void importUser(UserExportDTO reqDTO){

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUserName());

        int count = this.count(wrapper);

        if(count > 0){
            throw new ServiceException("????????????"+reqDTO.getUserName()+"??????????????????????????????");
        }


        // ????????????
        SysUser user = new SysUser();
        BeanMapper.copy(reqDTO, user);
        user.setId(IdWorker.getIdStr());

        String pass = reqDTO.getPassword();
        if(StringUtils.isBlank(pass)){
            pass = DEFAULT_PASS;
        }
        PassInfo passInfo = PassHandler.buildPassword(pass);
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());

        // ????????????
        List<String> roles = new ArrayList<>();
        if(StringUtils.isBlank(reqDTO.getRoleIds())){
            roles.add(ROLE_STUDENT);
        }else {
            // ????????????
            String [] roleIds = reqDTO.getRoleIds().split(",");
            roles = Arrays.asList(roleIds);
        }

        sysUserRoleService.saveRoles(user.getId(), roles);
        this.save(user);
    }

    /**
     * ??????????????????
     * @param user
     * @return
     */
    private SysUserLoginDTO setToken(SysUser user){

        // ?????????????????????????????????
        String key = Constant.USER_NAME_KEY+user.getUserName();
        String json = redisService.getString(key);
        if(!StringUtils.isBlank(json)){
            // ??????????????????
            redisService.del(key);
        }

        SysUserLoginDTO respDTO = new SysUserLoginDTO();
        BeanMapper.copy(user, respDTO);

        // ??????????????????Token
        String token = JwtUtils.sign(user.getUserName());
        respDTO.setToken(token);

        // ??????????????????
        this.fillRoleData(respDTO);

        // ????????????????????????????????????
        List<String> permissions = sysUserRoleService.findUserPermission(user.getId());
        respDTO.setPermissions(permissions);


        // ?????????Redis
        redisService.set(key, JSON.toJSONString(respDTO));

        return respDTO;

    }


    /**
     * ????????????????????????
     * @param respDTO
     */
    private void fillRoleData(SysUserLoginDTO respDTO){

        // ????????????
        List<SysRole> roleList = sysUserRoleService.listRoles(respDTO.getId());

        // ????????????1??????????????????????????????
        Integer dataScope = 1;
        // ????????????1????????????????????????????????????
        Integer roleType = 1;
        List<String> roleIds = new ArrayList<>();
        for(SysRole role: roleList){
            // ??????ID
            roleIds.add(role.getId());
            // ??????????????????
            if(dataScope < role.getDataScope()){
                dataScope = role.getDataScope();
            }
            // ????????????????????????
            if(roleType < role.getRoleType()){
                roleType = role.getRoleType();
            }
        }

        respDTO.setRoleType(roleType);
        respDTO.setDataScope(dataScope);
        respDTO.setRoles(roleIds);
    }

    @Override
    public SysUserLoginDTO syncLogin(String userName, String realName, String role, Long timestamp, String departs, String token) {


//        // 30?????????
//        Calendar cl = Calendar.getInstance();
//        cl.setTimeInMillis(timestamp * 1000L);
//        cl.add(Calendar.SECOND, 30);
//
//        if(cl.getTime().before(new Date())){
//            throw new ServiceException("token??????????????????");
//        }

        // ????????????
        boolean check = SignUtils.checkToken(userName, timestamp, token);
        if(!check){
            throw new ServiceException("?????????????????????");
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, userName);
        SysUser user = this.getOne(wrapper, false);

        // ?????????????????????
        if(user!=null){
            return this.checkAndLogin(user, null);
        }else{
            String deptCode = sysDepartService.syncDepart(departs);
            return this.saveAndLogin( userName,  deptCode,  realName, role, "", "", RandomStringUtils.randomAlphanumeric(16));
        }
    }
}
