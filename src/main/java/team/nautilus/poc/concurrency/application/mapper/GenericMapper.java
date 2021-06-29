package team.nautilus.poc.concurrency.application.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface GenericMapper<D,E> {

  E toEntity(D dto);
  D toDTO(E entity);

  default List<D> toDTOs(final Collection<E> entities) {
    return entities.stream()
      .map(this::toDTO)
      .collect(Collectors.toList());
  }

  default List<E> toEntities(final Collection<D> dtos) {
    return dtos.stream()
      .map(this::toEntity)
      .collect(Collectors.toList());
  }

}
