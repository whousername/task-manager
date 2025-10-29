package org.example.taskmanager.tasks;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;


public record Task(

        @Null
        Long id,

        @NotBlank(message = "Title must not be empty.")
        @NotNull
        @Size(max = 100, message = "Title must be at most 100 characters long.")
        String title,

        @Size(max = 500, message = "Description must be at most 500 characters.")
        String description,

        @NotNull
        @Positive
        Long creatorId,

        @NotNull
        @Positive
        Long assignedUserId,

        @Null
        Status status,

        @Null
        LocalDateTime createDateTime,

        @NotNull
        @Future//(message = "Deadline must be in the future")
        LocalDateTime deadLineDate,

        @NotNull
        Priority priority,

        @Null
        LocalDateTime doneDataTime

)
{


}


