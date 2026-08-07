-- Banana 权限资源初始化。
-- 资源定义由 Banana 维护，用户、角色及授权关系仍在 Watermelon 中配置。

INSERT INTO resource_node
    (name, type, code, state, remark, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT 'Banana 文件系统', 4, 'banana', 1, '', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM resource_node WHERE code = 'banana' AND is_deleted = 0
);

INSERT INTO resource_node
    (name, type, code, state, remark, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT '首页', 1, 'banana:home.page', 1, '', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM resource_node WHERE code = 'banana:home.page' AND is_deleted = 0
);

INSERT INTO resource_node
    (name, type, code, state, remark, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT 'OSS 管理页面', 1, 'banana:admin.oss.page', 1, '', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM resource_node WHERE code = 'banana:admin.oss.page' AND is_deleted = 0
);

INSERT INTO resource_node
    (name, type, code, state, remark, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT '新增 OSS 按钮', 2, 'banana:admin.oss.add.button', 1, '', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM resource_node WHERE code = 'banana:admin.oss.add.button' AND is_deleted = 0
);

INSERT INTO resource_node
    (name, type, code, state, remark, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT '新增 OSS 接口', 3, 'banana:POST:/api/admin/oss', 1, '', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM resource_node WHERE code = 'banana:POST:/api/admin/oss' AND is_deleted = 0
);

INSERT INTO resource_relation
    (parent_id, child_id, order_num, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT 0, child.id, 1, 1, NOW(), 1, NOW(), 0
FROM resource_node child
WHERE child.code = 'banana' AND child.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM resource_relation rr
      WHERE rr.parent_id = 0 AND rr.child_id = child.id AND rr.is_deleted = 0
  );

INSERT INTO resource_relation
    (parent_id, child_id, order_num, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT parent.id, child.id, 1, 1, NOW(), 1, NOW(), 0
FROM resource_node parent
JOIN resource_node child ON child.code = 'banana:home.page' AND child.is_deleted = 0
WHERE parent.code = 'banana' AND parent.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM resource_relation rr
      WHERE rr.parent_id = parent.id AND rr.child_id = child.id AND rr.is_deleted = 0
  );

INSERT INTO resource_relation
    (parent_id, child_id, order_num, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT parent.id, child.id, 2, 1, NOW(), 1, NOW(), 0
FROM resource_node parent
JOIN resource_node child ON child.code = 'banana:admin.oss.page' AND child.is_deleted = 0
WHERE parent.code = 'banana' AND parent.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM resource_relation rr
      WHERE rr.parent_id = parent.id AND rr.child_id = child.id AND rr.is_deleted = 0
  );

INSERT INTO resource_relation
    (parent_id, child_id, order_num, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT parent.id, child.id, 1, 1, NOW(), 1, NOW(), 0
FROM resource_node parent
JOIN resource_node child ON child.code = 'banana:admin.oss.add.button' AND child.is_deleted = 0
WHERE parent.code = 'banana:admin.oss.page' AND parent.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM resource_relation rr
      WHERE rr.parent_id = parent.id AND rr.child_id = child.id AND rr.is_deleted = 0
  );

INSERT INTO resource_relation
    (parent_id, child_id, order_num, created_by, created_time, updated_by, updated_time, is_deleted)
SELECT parent.id, child.id, 1, 1, NOW(), 1, NOW(), 0
FROM resource_node parent
JOIN resource_node child ON child.code = 'banana:POST:/api/admin/oss' AND child.is_deleted = 0
WHERE parent.code = 'banana:admin.oss.add.button' AND parent.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM resource_relation rr
      WHERE rr.parent_id = parent.id AND rr.child_id = child.id AND rr.is_deleted = 0
  );
