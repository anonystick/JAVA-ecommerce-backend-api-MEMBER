package com.myshop.modules.permission.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myshop.common.enums.ResultCode;
import com.myshop.common.exception.ServiceException;
import com.myshop.common.security.token.Token;
import com.myshop.modules.permission.entity.dos.AdminUser;
import com.myshop.modules.permission.mapper.AdminUserMapper;
import com.myshop.modules.permission.service.AdminUserService;
import com.myshop.modules.system.token.ManagerTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Autowired
    private ManagerTokenProvider managerTokenProvider;

    @Override
    public Token login(String username, String password) {
        // Tìm kiếm adminUser dựa trên username
        AdminUser adminUser = this.findByUsername(username);
        // Kiểm tra xem adminUser có null hoặc trạng thái không hợp lệ (không hoạt động)
        if (adminUser == null || !adminUser.getStatus()) {
            // Nếu adminUser không tồn tại hoặc không hoạt động, ném ngoại lệ ServiceException với mã lỗi USER_PASSWORD_ERROR
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
        // Kiểm tra xem mật khẩu nhập vào có khớp với mật khẩu đã mã hóa của adminUser
        if (!new BCryptPasswordEncoder().matches(password, adminUser.getPassword())) {
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
        try {
            // Tạo token cho adminUser và trả về
            return managerTokenProvider.createToken(adminUser, false);
        } catch (Exception e) {
            log.error("Administrator login error", e);
        }
        return null;

    }

    @Override
    public AdminUser findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username), false);
    }
}
