package ru.practicum.ewm_main.category.service;

import ru.practicum.ewm_main.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long id);

    CategoryDto updateCategory(CategoryDto categoryDto);

    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategory(Long id);
}
