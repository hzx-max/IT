package com.netconfig.service;

import com.netconfig.entity.Favorite;
import com.netconfig.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public List<Favorite> getByUserId(String userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public boolean isFavorite(String userId, String module, String itemId) {
        return favoriteRepository.existsByUserIdAndModuleAndItemId(userId, module, itemId);
    }

    @Transactional
    public boolean toggle(String userId, String module, String itemId,
                          String itemTitle, String moduleLabel,
                          String description, String category, String itemPath) {
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndModuleAndItemId(userId, module, itemId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // removed
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setModule(module);
            fav.setItemId(itemId);
            fav.setItemTitle(itemTitle);
            fav.setModuleLabel(moduleLabel);
            fav.setDescription(description);
            fav.setCategory(category);
            fav.setItemPath(itemPath);
            fav.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            favoriteRepository.save(fav);
            return true; // added
        }
    }

    @Transactional
    public void deleteBatch(String userId, List<Long> ids) {
        List<Favorite> toDelete = favoriteRepository.findByIdIn(ids);
        List<Long> ownedIds = new ArrayList<>();
        for (Favorite f : toDelete) {
            if (userId.equals(f.getUserId())) {
                ownedIds.add(f.getId());
            }
        }
        if (!ownedIds.isEmpty()) {
            favoriteRepository.deleteAllById(ownedIds);
        }
    }
}
