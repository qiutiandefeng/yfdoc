package com.yf.document.modules.user;
import com.yf.boot.base.api.api.ApiError;
import com.yf.boot.base.api.exception.ServiceException;
import com.yf.document.modules.sys.user.dto.response.SysUserLoginDTO;
import org.apache.shiro.SecurityUtils;
/**
 * 用户静态工具类
 * @author bool
 */
public class UserUtils {


    /**
     * 获取用户信息
     * @param throwable
     * @return
     */
    public static SysUserLoginDTO getUser(boolean throwable){
        try {
            return ((SysUserLoginDTO) SecurityUtils.getSubject().getPrincipal());
        }catch (Exception e){
            if(throwable){
                throw new ServiceException(ApiError.ERROR_10010002);
            }
            return null;
        }
    }

    /**
     * 获取当前登录用户的ID
     * @param throwable
     * @return
     */
    public static String getUserId(boolean throwable){
        SysUserLoginDTO user = getUser(throwable);
        if(user!=null){
            return user.getId();
        }
        return null;
    }

    /**
     * 是否管理员
     * @return
     */
    public static String departCode(){
        SysUserLoginDTO user = getUser(false);
        if(user!=null){
            return user.getDeptCode();
        }
        return null;
    }

    /**
     * 权限类型
     * @return
     */
    public static Integer getDataScope(){
        SysUserLoginDTO user = getUser(false);
        if(user!=null){
            return user.getDataScope();
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID，默认是会抛异常的
     * @return
     */
    public static String getUserId(){
        return getUserId(true);
    }
}
