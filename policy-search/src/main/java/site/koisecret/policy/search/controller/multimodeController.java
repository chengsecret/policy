package site.koisecret.policy.search.controller;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.BaseSearchDTO;
import site.koisecret.policy.search.dto.ModalitySearchDTO;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.entity.SearchHistory;
import site.koisecret.policy.search.mapper.SearchHistoryMapper;
import site.koisecret.policy.search.qiniu.QiniuServiceImpl;
import site.koisecret.policy.search.service.CollectionService;
import site.koisecret.policy.search.service.PolicyService;
import site.koisecret.policy.search.service.SmartService;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author by chengsecret
 * @date 2023/4/8.
 */
@RestController
public class multimodeController {

    @Autowired
    private QiniuServiceImpl qiniuServiceImpl;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    @Autowired
    private SmartService smartService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private PolicyService policyService;

    @PostMapping("/multimode/upload")
    @SneakyThrows
    public Result uploadImage(@RequestParam("image") MultipartFile image) {
        if (null == image || image.isEmpty()) {
            return Result.FAIL("文件为空");
        }
        if(image.getSize() > 3*1048576 ){  //大于3M
            return Result.FAIL("文件大小不能超过3MB");
        }
        return qiniuServiceImpl.uploadImage(image);
    }

    @SneakyThrows
    @PostMapping("/modality")
    public Result uploadImage(@RequestBody ModalitySearchDTO modalitySearchDTO, @RequestHeader("uid") String uid) {
        Page<Policy> page = new Page<>();
        page.setSize(modalitySearchDTO.getSize());
        page.setCurrent(modalitySearchDTO.getCurrent());

        if (StringUtils.isNotBlank(modalitySearchDTO.getValue())) {
            //保存搜索记录
            threadPoolExecutor.execute(() -> {
                SearchHistory searchHistory = new SearchHistory(-1, Integer.parseInt(uid),
                        null,
                        modalitySearchDTO.getValue(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        3  //3表示 多模态搜索
                );
                searchHistoryMapper.addSearchHistory(searchHistory);
            });
            BaseSearchDTO baseSearchDTO = new BaseSearchDTO();
            baseSearchDTO.setCurrent(modalitySearchDTO.getCurrent());
            baseSearchDTO.setSize(modalitySearchDTO.getSize());
            baseSearchDTO.setText(modalitySearchDTO.getValue());
            Result result = smartService.smartSearch(baseSearchDTO, null);
            return collectionService.collectionListHaveAuth(uid,result);

        }else {
            Result result = policyService.general(page);
            return collectionService.collectionListHaveAuth(uid,result);
        }
    }

}
