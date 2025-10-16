package org.example.taskmanager.model;

import org.example.taskmanager.Priority;
import org.example.taskmanager.Status;

import java.time.LocalDateTime;

public record Task(
        Long id,
        Long creatorId,
        Long assignedUserId,
        Status status,
        LocalDateTime createDateTime,
        LocalDateTime deadLineDate,
        Priority priority

)
{



}


//Создайте класс `Task`
//
//        - `id` (тип Long) - уникальный идентификатор
//
//  - `creatorId` (тип Long) - идентификатор пользователя, создавшего задачу
//
//  - `assignedUserId` (тип Long) - идентификатор исполнителя
//
//  - `status` -  статус задачи, представленный в виде enum с возможными значениями
//
//    - `CREATED` – задача создана, но ещё не взята в работу.
//
//    - `IN_PROGRESS` – задача находится в работе.
//
//        - `DONE` – задача завершена.
//
//        - `createDateTime` (тип LocalDateTime) - дата и время создания задачи
//
//  - `deadlineDate` (тип LocalDate или LocalDateTime) - дата, до которой задача должна быть выполнена
//
//  - `priority` (enum, например, `Low`, `Medium`, `High`).

