-- 水电账单功能：菜单与表注释更新
-- 学生端（user_type=0）菜单：水电账单
INSERT INTO tb_menu (id, title, icon, href, target, parent_id) VALUES (1042, '水电账单', NULL, 'billstu/list.html', NULL, 0);
-- 宿管端（user_type=1）菜单：账单推送
INSERT INTO tb_menu (id, title, icon, href, target, parent_id) VALUES (1122, '账单推送', NULL, 'bill/list.html', NULL, 0);
-- 角色与菜单关联
INSERT INTO tb_user_menu (id, user_type, menu_id) VALUES (22, 0, 1042);
INSERT INTO tb_user_menu (id, user_type, menu_id) VALUES (23, 1, 1122);
-- 更新 tb_bill.paid 列注释为新的状态语义
ALTER TABLE tb_bill MODIFY COLUMN paid int DEFAULT NULL COMMENT '账单状态：未生成=0（待推送）；已推送=1（待支付）；已支付=2';
