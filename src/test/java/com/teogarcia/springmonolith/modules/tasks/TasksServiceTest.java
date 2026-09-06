package com.teogarcia.springmonolith.modules.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.teogarcia.springmonolith.modules.tasks.dto.CreateTaskRequest;
import com.teogarcia.springmonolith.modules.tasks.dto.UpdateTaskRequest;
import com.teogarcia.springmonolith.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

  @Mock TaskRepository repository;

  @InjectMocks TasksService service;

  @Test
  void createAppliesDomainDefaultsBeforePersisting() {
    when(repository.saveAndFlush(any(Task.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.create(new CreateTaskRequest("Write tests", null, null, null));

    var task = ArgumentCaptor.forClass(Task.class);
    verify(repository).saveAndFlush(task.capture());
    assertThat(task.getValue().getId()).hasSize(16);
    assertThat(task.getValue().getStatus()).isEqualTo(TaskStatus.PENDING);
    assertThat(task.getValue().getPriority()).isZero();
    assertThat(response.title()).isEqualTo("Write tests");
    assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
  }

  @Test
  void findAllForwardsFiltersAndOneBasedPagination() {
    var stored = task("task-1", "Priority task", TaskStatus.IN_PROGRESS, 7);
    when(repository.findFiltered(any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(stored), Pageable.ofSize(1), 4));

    var response = service.findAll(TaskStatus.IN_PROGRESS, 5, 3, 10);

    var pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findFiltered(eq(TaskStatus.IN_PROGRESS), eq(5), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
    assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection())
        .isEqualTo(Sort.Direction.DESC);
    assertThat(response.data()).hasSize(1);
    assertThat(response.meta().total()).isEqualTo(4);
    assertThat(response.meta().page()).isEqualTo(3);
  }

  @Test
  void findOneRejectsMissingOrSoftDeletedTasks() {
    when(repository.findActiveById("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findOne("missing"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Task not found");
  }

  @Test
  void updateChangesOnlyFieldsPresentInTheRequest() {
    var stored = task("task-1", "Original", TaskStatus.PENDING, 3);
    when(repository.findActiveById(stored.getId())).thenReturn(Optional.of(stored));
    when(repository.saveAndFlush(stored)).thenReturn(stored);

    var response =
        service.update(
            stored.getId(),
            new UpdateTaskRequest(null, "New description", TaskStatus.COMPLETED, null));

    assertThat(stored.getTitle()).isEqualTo("Original");
    assertThat(stored.getDescription()).isEqualTo("New description");
    assertThat(stored.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    assertThat(stored.getPriority()).isEqualTo(3);
    assertThat(response.status()).isEqualTo(TaskStatus.COMPLETED);
    verify(repository).saveAndFlush(stored);
  }

  @Test
  void deleteIsARecoverableSoftDelete() {
    var stored = task("task-1", "Archive me", TaskStatus.PENDING, 0);
    when(repository.findActiveById(stored.getId())).thenReturn(Optional.of(stored));

    service.delete(stored.getId());

    assertThat(stored.getDeletedAt()).isNotNull();
    verify(repository).save(stored);
  }

  private static Task task(String id, String title, TaskStatus status, int priority) {
    return new Task(id, title, null, status, priority);
  }
}
