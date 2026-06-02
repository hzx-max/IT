package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.SearchHistory;
import com.netconfig.repository.SearchHistoryRepository;
import com.netconfig.service.JsonUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search-history")
public class SearchHistoryController {

    private final SearchHistoryRepository repo;

    public SearchHistoryController(SearchHistoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{module}")
    public ApiResponse<List<SearchHistory>> list(@PathVariable String module) {
        return ApiResponse.success(repo.findTop5ByModuleOrderBySearchedAtDesc(module));
    }

    @PostMapping
    public ApiResponse<Void> save(@RequestBody Map<String, String> body) {
        String module = body.get("module");
        String keyword = body.get("keyword");
        if (module == null || keyword == null || keyword.isBlank()) return ApiResponse.success();

        keyword = keyword.trim();
        if (keyword.isEmpty()) return ApiResponse.success();

        repo.deleteByModuleAndKeyword(module, keyword);

        SearchHistory sh = new SearchHistory();
        sh.setModule(module);
        sh.setKeyword(keyword);
        sh.setSearchedAt(JsonUtil.nowLocal());
        repo.save(sh);

        List<SearchHistory> all = repo.findTop5ByModuleOrderBySearchedAtDesc(module);
        if (all.size() > 5) {
            for (int i = 5; i < all.size(); i++) {
                repo.deleteById(all.get(i).getId());
            }
        }

        return ApiResponse.success();
    }

    @DeleteMapping("/{module}/{id}")
    public ApiResponse<Void> delete(@PathVariable String module, @PathVariable Long id) {
        repo.deleteById(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{module}")
    public ApiResponse<Void> clear(@PathVariable String module) {
        List<SearchHistory> all = repo.findTop5ByModuleOrderBySearchedAtDesc(module);
        for (SearchHistory sh : all) repo.deleteById(sh.getId());
        return ApiResponse.success();
    }
}
