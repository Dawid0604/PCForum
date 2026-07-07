package pl.dawid0604.pcforum.thread.service.core;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.dawid0604.pcforum.thread.service.commons.dto.CategoryThreadDto;
import pl.dawid0604.pcforum.thread.service.commons.dto.MatchedThreadDto;
import pl.dawid0604.pcforum.thread.service.commons.dto.ThreadDetailsDto;
import pl.dawid0604.pcforum.thread.service.commons.dto.UserThreadDto;
import pl.dawid0604.pcforum.thread.service.persistence.CategoryThread;
import pl.dawid0604.pcforum.thread.service.persistence.MatchedThread;
import pl.dawid0604.pcforum.thread.service.persistence.ThreadDetails;
import pl.dawid0604.pcforum.thread.service.persistence.UserThread;

@Mapper(
        componentModel = "spring",
        uses = {
                ThreadStatusConverter.class,
                ThreadDateTimeConverter.class
        }
)
interface ThreadEntityMapper {

    @Mapping(
            target = "isBanned",
            source = "status",
            qualifiedByName = "isBanned"
    )
    @Mapping(
            target = "isClosed",
            source = "status",
            qualifiedByName = "isClosed"
    )
    @Mapping(
            target = "createdDate",
            source = "createdDate",
            qualifiedByName = "instantToString"
    )
    ThreadDetailsDto toDetailsDto(ThreadDetails thread);

    @Mapping(
            target = "isPinned",
            source = "status",
            qualifiedByName = "isPinned"
    )
    @Mapping(
            target = "isClosed",
            source = "status",
            qualifiedByName = "isClosed"
    )
    @Mapping(
            target = "createdDate",
            source = "createdDate",
            qualifiedByName = "instantToString"
    )
    CategoryThreadDto toCategoryDto(CategoryThread thread);

    @Mapping(
            target = "isBanned",
            source = "status",
            qualifiedByName = "isBanned"
    )
    @Mapping(
            target = "isPinned",
            source = "status",
            qualifiedByName = "isPinned"
    )
    @Mapping(
            target = "isClosed",
            source = "status",
            qualifiedByName = "isClosed"
    )
    @Mapping(
            target = "createdDate",
            source = "createdDate",
            qualifiedByName = "instantToString"
    )
    UserThreadDto toUserDto(UserThread thread);

    @Mapping(
            target = "isClosed",
            source = "status",
            qualifiedByName = "isClosed"
    )
    @Mapping(
            target = "isPinned",
            source = "status",
            qualifiedByName = "isPinned"
    )
    @Mapping(
            target = "createdDate",
            source = "createdDate",
            qualifiedByName = "instantToString"
    )
    MatchedThreadDto toMatchedDto(MatchedThread thread);
}
