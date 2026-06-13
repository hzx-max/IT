package com.netconfig.service;

import com.netconfig.entity.UserProfile;
import com.netconfig.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    public Optional<UserProfile> getByUserId(String userId) {
        return profileRepository.findByUserId(userId);
    }

    @Transactional
    public UserProfile saveOrUpdate(String userId, String realName, String email, String avatar, String bio) {
        Optional<UserProfile> existing = profileRepository.findByUserId(userId);
        UserProfile profile;
        if (existing.isPresent()) {
            profile = existing.get();
        } else {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(now());
        }
        if (realName != null) profile.setRealName(realName);
        if (email != null) profile.setEmail(email);
        if (avatar != null) profile.setAvatar(avatar);
        if (bio != null) profile.setBio(bio);
        profile.setUpdatedAt(now());
        return profileRepository.save(profile);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
