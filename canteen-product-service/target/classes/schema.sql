-- 创建商品分类表
CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    merchant_id BIGINT COMMENT '商家ID',
    image_url VARCHAR(500) COMMENT '图片URL',
    stock INT DEFAULT 0 COMMENT '库存数量',
    sales INT DEFAULT 0 COMMENT '销售数量',
    status TINYINT DEFAULT 1 COMMENT '状态：1-上架，0-下架',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门：1-是，0-否',
    rating DECIMAL(3,2) DEFAULT 0.0 COMMENT '商品评分',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES product_category(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);
CREATE INDEX IF NOT EXISTS idx_category_status ON product_category(status);