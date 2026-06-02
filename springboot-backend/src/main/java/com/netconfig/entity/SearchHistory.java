package com.netconfig.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String module;
    @Column(nullable = false)
    private String keyword;
    @Column(name = "searched_at", nullable = false)
    private String searchedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getSearchedAt() { return searchedAt; }
    public void setSearchedAt(String searchedAt) { this.searchedAt = searchedAt; }
}
