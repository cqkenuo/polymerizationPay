package com.polymerization.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.dto.AppDTO;
import com.polymerization.merchant.convert.AppCovert;
import com.polymerization.merchant.entity.App;
import com.polymerization.merchant.entity.Merchant;
import com.polymerization.merchant.mapper.AppMapper;
import com.polymerization.merchant.mapper.MerchantMapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RandomStringUtil;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AppServiceImpl implements AppService {
    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 商户下创建应用
     *
     * @param merchantId
     * @param appDTO
     * @return
     */
    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {
        if (merchantId == null || appDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        if(isExistAppName(appDTO.getAppName())) {
            throw new BusinessException(CommonErrorCode.E_200004);
        }

        //保存应用信息
        appDTO.setAppId(RandomStringUtil.getRandomString(32));
        appDTO.setMerchantId(merchantId);
        App entity = AppCovert.INSTANCE.dto2App(appDTO);
        appMapper.insert(entity);
        return AppCovert.INSTANCE.entity2DTO(entity);
    }

    /**
     * 根据业务id查询应用
     *
     * @return
     * @throws BusinessException
     */
    @Override
    public AppDTO getAppById(String id) throws BusinessException {
        App app = appMapper.selectById(id);
        AppDTO appDTO = AppCovert.INSTANCE.entity2DTO(app);
        return appDTO;
    }

    /**
     * 查询商户的应用列表
     *
     * @param merchantId
     * @return
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) {
        List<App> appList = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        return AppCovert.INSTANCE.listEntity2DTO(appList);
    }

    /**
     * 校验应用名是否被使用
     * @param appName
     * @return
     */
    private boolean isExistAppName(String appName) {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, appName));
        return count > 0;
    }
}
