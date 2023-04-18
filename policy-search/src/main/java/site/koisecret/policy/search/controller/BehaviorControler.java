package site.koisecret.policy.search.controller;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.BehaviorDTO;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.service.BehaviorService;
import site.koisecret.policy.search.service.CollectionService;

/**
 * @author by chengsecret
 * @date 2023/4/6.
 */
@RestController
public class BehaviorControler {
    @Autowired
    private BehaviorService behaviorService;
    @Autowired
    private CollectionService collectionService;

    @GetMapping("/behavior/get")
    @SneakyThrows
    Result getBehavior(@RequestHeader("uid") String uid, Page<Policy> page) {
        Result result = behaviorService.browseRecord(Integer.parseInt(uid), page);
        return collectionService.collectionListHaveAuth(uid, result);
    }

    @PostMapping("/behavior/add")
    Result addBehavior(@RequestHeader("uid") String uid, @RequestBody BehaviorDTO behaviorDTO) {
        return behaviorService.addBehavior(Integer.parseInt(uid), behaviorDTO.getPid());
    }
}
