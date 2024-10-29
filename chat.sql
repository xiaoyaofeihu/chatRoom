CREATE TABLE `config_complaint` (
                                    `orderNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                    `complainant` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投诉人',
                                    `complaints` varchar(50) DEFAULT NULL COMMENT '投诉事件',
                                    `complaintDetails` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '投诉详细内容',
                                    `state` bigint DEFAULT NULL COMMENT '状态(0：开始解决，1：解决中，2：已解决)',
                                    `acceptor` varchar(50) DEFAULT NULL COMMENT '受理人',
                                    `processingTime` datetime DEFAULT NULL COMMENT '受理时间',
                                    `picture` varchar(500) DEFAULT NULL COMMENT '图片',
                                    `creator` varchar(50) DEFAULT NULL COMMENT '创建人',
                                    `create_date` datetime DEFAULT NULL COMMENT '创建时间',
                                    `editor` varchar(50) DEFAULT NULL COMMENT '编辑人',
                                    `editor_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '编辑人id',
                                    `editor_date` datetime DEFAULT NULL COMMENT '编辑时间',
                                    `complainant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '投诉人ID',
                                    PRIMARY KEY (`orderNumber`) USING BTREE,
                                    KEY `complainant_index` (`complainant`) USING BTREE COMMENT '投诉人索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `config_files` (
                                `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
                                `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件名称',
                                `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
                                `size` bigint DEFAULT NULL COMMENT '文件大小',
                                `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下载链接',
                                `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                                `md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'md5',
                                `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归属人',
                                `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                PRIMARY KEY (`id`,`url`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=321 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `config_message` (
                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
                                  `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户name',
                                  `user_type` tinyint(1) DEFAULT NULL COMMENT '用户类型：1为客户，2为客服',
                                  `type` tinyint(1) DEFAULT '0' COMMENT '消息类型：0为文本，1为文件，2为图片',
                                  `room_id` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '房间id',
                                  `status` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态',
                                  `content` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '消息',
                                  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `created_user` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建房间者',
                                  PRIMARY KEY (`id`,`user_id`) USING BTREE,
                                  KEY `status` (`status`) USING BTREE COMMENT '状态索引'
) ENGINE=InnoDB AUTO_INCREMENT=1005055537827545089 DEFAULT CHARSET=utf8mb3;