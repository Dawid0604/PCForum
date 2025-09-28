package pl.dawid0604.pcforum.category.service.core;

import org.mapstruct.Mapper;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryWrapperDto;
import pl.dawid0604.pcforum.category.service.persistence.CategoryEntity;

import java.util.List;

import static java.util.Collections.emptyList;

@Mapper(componentModel = "spring")
interface CategoryEntityMapper {

    default CategoryWrapperDto wrap(final CategoryEntity categoryEntity, final List<CategoryWrapperDto> subCategories) {
        return new CategoryWrapperDto(toCategoryDto(categoryEntity), subCategories);
    }

    default CategoryWrapperDto wrap(final CategoryEntity categoryEntity) {
        return new CategoryWrapperDto(toCategoryDto(categoryEntity), emptyList());
    }

    CategoryDto toCategoryDto(CategoryEntity categoryEntity);
}
