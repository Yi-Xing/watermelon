INSERT INTO user (
    `id`,
    `name`,
    `email`,
    `phone`,
    `password`,
    `state`,
    `remark`,
    `created_by`,
    `created_time`,
    `updated_by`,
    `updated_time`,
    `is_deleted`
) VALUES (
          1,
             '超级管理员',
             'admin@fblue.top',
             '',
             '$2a$10$g4AnJ/GIbU7SepyybORu/.rMLXOCxs2.hyk0UEwKFJGxlf.X7Ocwa',
             1,
             '',
             1,
             NOW(),
             1,
             NOW(),
             0
         );
