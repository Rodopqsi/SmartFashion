Enter password: 
-- MySQL dump 10.13  Distrib 8.0.43, for Linux (x86_64)
--
-- Host: host.docker.internal    Database: smarthfashion
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `aplicacion_promocion`
--

DROP TABLE IF EXISTS `aplicacion_promocion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `aplicacion_promocion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_promocion` bigint unsigned NOT NULL,
  `id_producto` bigint unsigned DEFAULT NULL,
  `id_categoria` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `aplicacion_promocion_id_promocion_foreign` (`id_promocion`),
  KEY `aplicacion_promocion_id_producto_foreign` (`id_producto`),
  KEY `aplicacion_promocion_id_categoria_foreign` (`id_categoria`),
  CONSTRAINT `aplicacion_promocion_id_categoria_foreign` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id`),
  CONSTRAINT `aplicacion_promocion_id_producto_foreign` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`),
  CONSTRAINT `aplicacion_promocion_id_promocion_foreign` FOREIGN KEY (`id_promocion`) REFERENCES `promocion` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aplicacion_promocion`
--

LOCK TABLES `aplicacion_promocion` WRITE;
/*!40000 ALTER TABLE `aplicacion_promocion` DISABLE KEYS */;
/*!40000 ALTER TABLE `aplicacion_promocion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_group`
--

DROP TABLE IF EXISTS `auth_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_group`
--

LOCK TABLES `auth_group` WRITE;
/*!40000 ALTER TABLE `auth_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_group_permissions`
--

DROP TABLE IF EXISTS `auth_group_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_group_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_group_permissions_group_id_permission_id_0cd325b0_uniq` (`group_id`,`permission_id`),
  KEY `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` (`permission_id`),
  CONSTRAINT `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `auth_group_permissions_group_id_b120cbf9_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_group_permissions`
--

LOCK TABLES `auth_group_permissions` WRITE;
/*!40000 ALTER TABLE `auth_group_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_permission`
--

DROP TABLE IF EXISTS `auth_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type_id` int NOT NULL,
  `codename` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_permission_content_type_id_codename_01ab375a_uniq` (`content_type_id`,`codename`),
  CONSTRAINT `auth_permission_content_type_id_2f476e4b_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_permission`
--

LOCK TABLES `auth_permission` WRITE;
/*!40000 ALTER TABLE `auth_permission` DISABLE KEYS */;
INSERT INTO `auth_permission` VALUES (1,'Can add log entry',1,'add_logentry'),(2,'Can change log entry',1,'change_logentry'),(3,'Can delete log entry',1,'delete_logentry'),(4,'Can view log entry',1,'view_logentry'),(5,'Can add permission',2,'add_permission'),(6,'Can change permission',2,'change_permission'),(7,'Can delete permission',2,'delete_permission'),(8,'Can view permission',2,'view_permission'),(9,'Can add group',3,'add_group'),(10,'Can change group',3,'change_group'),(11,'Can delete group',3,'delete_group'),(12,'Can view group',3,'view_group'),(13,'Can add user',4,'add_user'),(14,'Can change user',4,'change_user'),(15,'Can delete user',4,'delete_user'),(16,'Can view user',4,'view_user'),(17,'Can add content type',5,'add_contenttype'),(18,'Can change content type',5,'change_contenttype'),(19,'Can delete content type',5,'delete_contenttype'),(20,'Can view content type',5,'view_contenttype'),(21,'Can add session',6,'add_session'),(22,'Can change session',6,'change_session'),(23,'Can delete session',6,'delete_session'),(24,'Can view session',6,'view_session'),(25,'Can add product review',7,'add_productreview'),(26,'Can change product review',7,'change_productreview'),(27,'Can delete product review',7,'delete_productreview'),(28,'Can view product review',7,'view_productreview'),(29,'Can add categoria',8,'add_categoria'),(30,'Can change categoria',8,'change_categoria'),(31,'Can delete categoria',8,'delete_categoria'),(32,'Can view categoria',8,'view_categoria'),(33,'Can add imagen producto',9,'add_imagenproducto'),(34,'Can change imagen producto',9,'change_imagenproducto'),(35,'Can delete imagen producto',9,'delete_imagenproducto'),(36,'Can view imagen producto',9,'view_imagenproducto'),(37,'Can add producto',10,'add_producto'),(38,'Can change producto',10,'change_producto'),(39,'Can delete producto',10,'delete_producto'),(40,'Can view producto',10,'view_producto'),(41,'Can add variacion producto',11,'add_variacionproducto'),(42,'Can change variacion producto',11,'change_variacionproducto'),(43,'Can delete variacion producto',11,'delete_variacionproducto'),(44,'Can view variacion producto',11,'view_variacionproducto'),(45,'Can add user address',12,'add_useraddress'),(46,'Can change user address',12,'change_useraddress'),(47,'Can delete user address',12,'delete_useraddress'),(48,'Can view user address',12,'view_useraddress'),(49,'Can add complaint',13,'add_complaint'),(50,'Can change complaint',13,'change_complaint'),(51,'Can delete complaint',13,'delete_complaint'),(52,'Can view complaint',13,'view_complaint'),(53,'Can add return request',14,'add_returnrequest'),(54,'Can change return request',14,'change_returnrequest'),(55,'Can delete return request',14,'delete_returnrequest'),(56,'Can view return request',14,'view_returnrequest'),(57,'Can add coleccion',15,'add_coleccion'),(58,'Can change coleccion',15,'change_coleccion'),(59,'Can delete coleccion',15,'delete_coleccion'),(60,'Can view coleccion',15,'view_coleccion'),(61,'Can add coleccion producto',16,'add_coleccionproducto'),(62,'Can change coleccion producto',16,'change_coleccionproducto'),(63,'Can delete coleccion producto',16,'delete_coleccionproducto'),(64,'Can view coleccion producto',16,'view_coleccionproducto');
/*!40000 ALTER TABLE `auth_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user`
--

DROP TABLE IF EXISTS `auth_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `username` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(254) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `date_joined` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user`
--

LOCK TABLES `auth_user` WRITE;
/*!40000 ALTER TABLE `auth_user` DISABLE KEYS */;
INSERT INTO `auth_user` VALUES (1,'!A5Avs2FObAstqzzkRG5Bh99PBFfim2EEZZuRqdMs',NULL,0,'Rodop','','','yasstavera@gmail.com',0,1,'2025-11-12 17:23:07.126785'),(2,'pbkdf2_sha256$720000$4yJFp4hXgxgtcbcTsCCccE$JJ1ql+/htLT/cHxQiAaH+jbEFH5X03vK8duj/5TZCek=',NULL,0,'Rodoo','','','rodolfo.tavera@tecsup.edu.pe',0,1,'2025-11-18 16:12:38.804749');
/*!40000 ALTER TABLE `auth_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_groups`
--

DROP TABLE IF EXISTS `auth_user_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_user_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `group_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_user_groups_user_id_group_id_94350c0c_uniq` (`user_id`,`group_id`),
  KEY `auth_user_groups_group_id_97559544_fk_auth_group_id` (`group_id`),
  CONSTRAINT `auth_user_groups_group_id_97559544_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`),
  CONSTRAINT `auth_user_groups_user_id_6a12ed8b_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_groups`
--

LOCK TABLES `auth_user_groups` WRITE;
/*!40000 ALTER TABLE `auth_user_groups` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_user_permissions`
--

DROP TABLE IF EXISTS `auth_user_user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_user_user_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_user_user_permissions_user_id_permission_id_14a6b632_uniq` (`user_id`,`permission_id`),
  KEY `auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm` (`permission_id`),
  CONSTRAINT `auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `auth_user_user_permissions_user_id_a95ead1b_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_user_permissions`
--

LOCK TABLES `auth_user_user_permissions` WRITE;
/*!40000 ALTER TABLE `auth_user_user_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user_user_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carrito`
--

DROP TABLE IF EXISTS `carrito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrito` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `estado` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `carrito_id_usuario_foreign` (`id_usuario`),
  CONSTRAINT `carrito_id_usuario_foreign` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrito`
--

LOCK TABLES `carrito` WRITE;
/*!40000 ALTER TABLE `carrito` DISABLE KEYS */;
/*!40000 ALTER TABLE `carrito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (1,'Hombres'),(2,'Polos'),(3,'Camisas'),(4,'Pantalones'),(5,'Chaquetas'),(6,'Vestidos'),(7,'Zapatillas'),(8,'Sudaderas'),(9,'Faldas'),(10,'Blusas'),(11,'Shorts'),(12,'Poleras'),(13,'Cardigans'),(14,'Camisetas');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `centrodistribucion`
--

DROP TABLE IF EXISTS `centrodistribucion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `centrodistribucion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `region` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `centrodistribucion`
--

LOCK TABLES `centrodistribucion` WRITE;
/*!40000 ALTER TABLE `centrodistribucion` DISABLE KEYS */;
INSERT INTO `centrodistribucion` VALUES (1,'Rodolfo','La Libertad');
/*!40000 ALTER TABLE `centrodistribucion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coleccion`
--

DROP TABLE IF EXISTS `coleccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coleccion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `descripcion` text,
  `image_url` varchar(1024) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `orden` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coleccion`
--

LOCK TABLES `coleccion` WRITE;
/*!40000 ALTER TABLE `coleccion` DISABLE KEYS */;
INSERT INTO `coleccion` VALUES (1,'Primavera-2025','primavera-2025','ss','https://bassika.pe/cdn/shop/files/BANNER_FW25_12eba9a8-e7b4-41b8-bf88-ed00411bf85c.jpg?v=1746551763',1,1);
/*!40000 ALTER TABLE `coleccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coleccionproducto`
--

DROP TABLE IF EXISTS `coleccionproducto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coleccionproducto` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_coleccion` bigint unsigned NOT NULL,
  `id_producto` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_coleccion_producto` (`id_coleccion`,`id_producto`),
  KEY `coleccion_producto_producto_fk` (`id_producto`),
  CONSTRAINT `coleccion_producto_coleccion_fk` FOREIGN KEY (`id_coleccion`) REFERENCES `coleccion` (`id`) ON DELETE CASCADE,
  CONSTRAINT `coleccion_producto_producto_fk` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coleccionproducto`
--

LOCK TABLES `coleccionproducto` WRITE;
/*!40000 ALTER TABLE `coleccionproducto` DISABLE KEYS */;
INSERT INTO `coleccionproducto` VALUES (1,1,1);
/*!40000 ALTER TABLE `coleccionproducto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `colores`
--

DROP TABLE IF EXISTS `colores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `colores` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `codigo_hex` char(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `colores`
--

LOCK TABLES `colores` WRITE;
/*!40000 ALTER TABLE `colores` DISABLE KEYS */;
INSERT INTO `colores` VALUES (1,'Negro','#000'),(2,'Rojo','#FF0000'),(3,'Blanco','#FFFFFF'),(4,'Gris','#6b7280'),(5,'Azul Marino','#1e3a8a'),(6,'Verde Olivo','#4d7c0f'),(7,'Khaki','#b59b6b'),(8,'Beige','#f5deb3'),(9,'Rosa','#f472b6');
/*!40000 ALTER TABLE `colores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complaints`
--

DROP TABLE IF EXISTS `complaints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaints` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_email` varchar(255) NOT NULL,
  `order_number` varchar(64) NOT NULL,
  `tipo` varchar(16) NOT NULL,
  `detalle` text NOT NULL,
  `estado` varchar(32) NOT NULL DEFAULT 'registrado',
  `respuesta` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_complaints_email` (`user_email`),
  KEY `idx_complaints_order` (`order_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complaints`
--

LOCK TABLES `complaints` WRITE;
/*!40000 ALTER TABLE `complaints` DISABLE KEYS */;
/*!40000 ALTER TABLE `complaints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_pedido`
--

DROP TABLE IF EXISTS `detalle_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_pedido` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_pedido` bigint unsigned NOT NULL,
  `id_variacion_producto` bigint unsigned NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(8,2) NOT NULL,
  `descuento_aplicado` bigint NOT NULL,
  `id_promocion_aplicada` bigint unsigned NOT NULL,
  `subtotal` decimal(8,2) NOT NULL COMMENT 'Campo calculado: (cantidad * precio_unitario) - descuento_aplicado.',
  PRIMARY KEY (`id`),
  KEY `detalle_pedido_id_promocion_aplicada_foreign` (`id_promocion_aplicada`),
  KEY `detalle_pedido_id_pedido_foreign` (`id_pedido`),
  KEY `detalle_pedido_id_variacion_producto_foreign` (`id_variacion_producto`),
  CONSTRAINT `detalle_pedido_id_pedido_foreign` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id`),
  CONSTRAINT `detalle_pedido_id_promocion_aplicada_foreign` FOREIGN KEY (`id_promocion_aplicada`) REFERENCES `promocion` (`id`),
  CONSTRAINT `detalle_pedido_id_variacion_producto_foreign` FOREIGN KEY (`id_variacion_producto`) REFERENCES `variaciones_producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_pedido`
--

LOCK TABLES `detalle_pedido` WRITE;
/*!40000 ALTER TABLE `detalle_pedido` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devolucion`
--

DROP TABLE IF EXISTS `devolucion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devolucion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(64) NOT NULL,
  `email` varchar(255) NOT NULL,
  `telefono` varchar(64) DEFAULT NULL,
  `motivo` varchar(64) NOT NULL,
  `descripcion` text,
  `metodo` varchar(16) NOT NULL,
  `estado` varchar(32) NOT NULL DEFAULT 'solicitado',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devolucion`
--

LOCK TABLES `devolucion` WRITE;
/*!40000 ALTER TABLE `devolucion` DISABLE KEYS */;
/*!40000 ALTER TABLE `devolucion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devolucionitem`
--

DROP TABLE IF EXISTS `devolucionitem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devolucionitem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `devolucion_id` bigint NOT NULL,
  `product_sku` varchar(64) DEFAULT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `condicion` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `devolucion_item_fk` (`devolucion_id`),
  CONSTRAINT `devolucion_item_fk` FOREIGN KEY (`devolucion_id`) REFERENCES `devolucion` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devolucionitem`
--

LOCK TABLES `devolucionitem` WRITE;
/*!40000 ALTER TABLE `devolucionitem` DISABLE KEYS */;
/*!40000 ALTER TABLE `devolucionitem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_admin_log`
--

DROP TABLE IF EXISTS `django_admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_admin_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `action_time` datetime(6) NOT NULL,
  `object_id` longtext COLLATE utf8mb4_unicode_ci,
  `object_repr` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action_flag` smallint unsigned NOT NULL,
  `change_message` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type_id` int DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `django_admin_log_content_type_id_c4bce8eb_fk_django_co` (`content_type_id`),
  KEY `django_admin_log_user_id_c564eba6_fk_auth_user_id` (`user_id`),
  CONSTRAINT `django_admin_log_content_type_id_c4bce8eb_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`),
  CONSTRAINT `django_admin_log_user_id_c564eba6_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`),
  CONSTRAINT `django_admin_log_chk_1` CHECK ((`action_flag` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_admin_log`
--

LOCK TABLES `django_admin_log` WRITE;
/*!40000 ALTER TABLE `django_admin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_admin_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_content_type`
--

DROP TABLE IF EXISTS `django_content_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_content_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_label` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `django_content_type_app_label_model_76bd3d3b_uniq` (`app_label`,`model`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_content_type`
--

LOCK TABLES `django_content_type` WRITE;
/*!40000 ALTER TABLE `django_content_type` DISABLE KEYS */;
INSERT INTO `django_content_type` VALUES (1,'admin','logentry'),(3,'auth','group'),(2,'auth','permission'),(4,'auth','user'),(5,'contenttypes','contenttype'),(6,'sessions','session'),(8,'shop','categoria'),(15,'shop','coleccion'),(16,'shop','coleccionproducto'),(13,'shop','complaint'),(9,'shop','imagenproducto'),(10,'shop','producto'),(7,'shop','productreview'),(14,'shop','returnrequest'),(12,'shop','useraddress'),(11,'shop','variacionproducto');
/*!40000 ALTER TABLE `django_content_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_migrations`
--

DROP TABLE IF EXISTS `django_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_migrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_migrations`
--

LOCK TABLES `django_migrations` WRITE;
/*!40000 ALTER TABLE `django_migrations` DISABLE KEYS */;
INSERT INTO `django_migrations` VALUES (1,'contenttypes','0001_initial','2025-11-12 17:15:52.514119'),(2,'auth','0001_initial','2025-11-12 17:15:52.897383'),(3,'admin','0001_initial','2025-11-12 17:15:52.992055'),(4,'admin','0002_logentry_remove_auto_add','2025-11-12 17:15:52.997163'),(5,'admin','0003_logentry_add_action_flag_choices','2025-11-12 17:15:53.001549'),(6,'contenttypes','0002_remove_content_type_name','2025-11-12 17:15:53.112586'),(7,'auth','0002_alter_permission_name_max_length','2025-11-12 17:15:53.156975'),(8,'auth','0003_alter_user_email_max_length','2025-11-12 17:15:53.183279'),(9,'auth','0004_alter_user_username_opts','2025-11-12 17:15:53.188259'),(10,'auth','0005_alter_user_last_login_null','2025-11-12 17:15:53.251789'),(11,'auth','0006_require_contenttypes_0002','2025-11-12 17:15:53.254390'),(12,'auth','0007_alter_validators_add_error_messages','2025-11-12 17:15:53.259530'),(13,'auth','0008_alter_user_username_max_length','2025-11-12 17:15:53.313145'),(14,'auth','0009_alter_user_last_name_max_length','2025-11-12 17:15:53.365757'),(15,'auth','0010_alter_group_name_max_length','2025-11-12 17:15:53.382647'),(16,'auth','0011_update_proxy_permissions','2025-11-12 17:15:53.389385'),(17,'auth','0012_alter_user_first_name_max_length','2025-11-12 17:15:53.441178'),(18,'sessions','0001_initial','2025-11-12 17:15:53.467608'),(19,'shop','0001_product_reviews','2025-11-12 17:15:53.480323'),(20,'shop','0002_categoria_imagenproducto_producto_variacionproducto','2025-11-12 17:15:53.483854'),(21,'shop','0003_useraddress','2025-11-12 17:15:53.487436'),(22,'shop','0004_claims_returns','2025-11-12 17:17:37.169021'),(23,'shop','0005_coleccion_coleccionproducto','2025-11-12 17:18:02.447611');
/*!40000 ALTER TABLE `django_migrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_session`
--

DROP TABLE IF EXISTS `django_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_session` (
  `session_key` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_data` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `expire_date` datetime(6) NOT NULL,
  PRIMARY KEY (`session_key`),
  KEY `django_session_expire_date_a5c62663` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_session`
--

LOCK TABLES `django_session` WRITE;
/*!40000 ALTER TABLE `django_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empresaenvio`
--

DROP TABLE IF EXISTS `empresaenvio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresaenvio` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cobertura` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tracking_url_base` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empresaenvio`
--

LOCK TABLES `empresaenvio` WRITE;
/*!40000 ALTER TABLE `empresaenvio` DISABLE KEYS */;
INSERT INTO `empresaenvio` VALUES (1,'Rodo envios','Lima','https://mail.google.com/mail/u/0/#inbox',1);
/*!40000 ALTER TABLE `empresaenvio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `envio`
--

DROP TABLE IF EXISTS `envio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `envio` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_centro_distribucion` bigint unsigned NOT NULL,
  `id_empresa_envio` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `destinatario` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `direccion` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email_destino` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefono_destino` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region_destino` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `codigo_tracking` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `costo_envio` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `envio_centro_fk` (`id_centro_distribucion`),
  KEY `envio_empresa_fk` (`id_empresa_envio`),
  CONSTRAINT `envio_centro_fk` FOREIGN KEY (`id_centro_distribucion`) REFERENCES `centrodistribucion` (`id`),
  CONSTRAINT `envio_empresa_fk` FOREIGN KEY (`id_empresa_envio`) REFERENCES `empresaenvio` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `envio`
--

LOCK TABLES `envio` WRITE;
/*!40000 ALTER TABLE `envio` DISABLE KEYS */;
INSERT INTO `envio` VALUES (1,'TEST-SF-123',1,1,'EN_TRANSITO','Juan','Calle Falsa 123','juan@example.com','99999999','La Libertad',NULL,'2025-11-22 14:32:41',NULL),(2,'SF1763804116',1,1,'EN_TRANSITO','Rodolfo Tavera','Avenida America','rodolfo.tavera@tecsup.edu.pe','917364262','Otro',NULL,'2025-11-22 14:35:35',NULL),(3,'SF1763804116',1,1,'EN_REPARTO','Rodolfo Tavera','Avenida America','rodolfo.tavera@tecsup.edu.pe','917364262','Otro',NULL,'2025-11-22 14:35:35',NULL);
/*!40000 ALTER TABLE `envio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eventoenvio`
--

DROP TABLE IF EXISTS `eventoenvio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eventoenvio` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_envio` bigint unsigned NOT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `nota` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `evento_envio_envio_fk` (`id_envio`),
  CONSTRAINT `evento_envio_envio_fk` FOREIGN KEY (`id_envio`) REFERENCES `envio` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eventoenvio`
--

LOCK TABLES `eventoenvio` WRITE;
/*!40000 ALTER TABLE `eventoenvio` DISABLE KEYS */;
INSERT INTO `eventoenvio` VALUES (1,1,'ASIGNADO','2025-11-22 14:32:41','Env├¡o creado desde checkout ┬À juan@example.com ┬À 99999999'),(2,2,'ASIGNADO','2025-11-22 14:35:35','Env├¡o creado desde checkout ┬À rodolfo.tavera@tecsup.edu.pe ┬À 917364262'),(3,3,'ASIGNADO','2025-11-22 14:35:35','Env├¡o creado desde checkout ┬À rodolfo.tavera@tecsup.edu.pe ┬À 917364262'),(4,1,'ENTREGADO','2025-11-22 14:35:57',''),(5,1,'EN_TRANSITO','2025-11-22 14:36:03',''),(6,3,'EN_REPARTO','2025-11-22 16:36:54',''),(7,2,'EN_TRANSITO','2025-11-22 16:40:36',''),(8,2,'EN_TRANSITO','2025-11-22 17:03:06','');
/*!40000 ALTER TABLE `eventoenvio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorito`
--

DROP TABLE IF EXISTS `favorito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorito` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `id_producto` bigint unsigned NOT NULL,
  `fecha_agregado` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `favorito_id_usuario_foreign` (`id_usuario`),
  KEY `favorito_id_producto_foreign` (`id_producto`),
  CONSTRAINT `favorito_id_producto_foreign` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`),
  CONSTRAINT `favorito_id_usuario_foreign` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorito`
--

LOCK TABLES `favorito` WRITE;
/*!40000 ALTER TABLE `favorito` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `imagenes_producto`
--

DROP TABLE IF EXISTS `imagenes_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imagenes_producto` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_color` bigint unsigned DEFAULT NULL,
  `id_talla` bigint unsigned DEFAULT NULL,
  `url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_producto` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `imagenes_producto_id_producto_foreign` (`id_producto`),
  KEY `imagenes_producto_id_color_foreign` (`id_color`),
  KEY `imagenes_producto_id_talla_foreign` (`id_talla`),
  CONSTRAINT `imagenes_producto_id_color_foreign` FOREIGN KEY (`id_color`) REFERENCES `colores` (`id`),
  CONSTRAINT `imagenes_producto_id_producto_foreign` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`),
  CONSTRAINT `imagenes_producto_id_talla_foreign` FOREIGN KEY (`id_talla`) REFERENCES `tallas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `imagenes_producto`
--

LOCK TABLES `imagenes_producto` WRITE;
/*!40000 ALTER TABLE `imagenes_producto` DISABLE KEYS */;
INSERT INTO `imagenes_producto` VALUES (1,1,1,'https://rematexperu.com/cdn/shop/products/30_313b4046-564b-4ed0-81ff-36a14415821c_800x.png?v=1668045565',1),(3,2,2,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTj2f2uctoAEYX1Tn1SUuidx3KRZoOKHWzSxg&s',1),(4,1,NULL,'/img/products/polo-classic/negro.jpg',2),(5,3,NULL,'/img/products/polo-classic/blanco.jpg',2),(6,4,NULL,'/img/products/polo-classic/gris.jpg',2),(7,5,NULL,'/img/products/polo-classic/azul-marino.jpg',2),(11,3,NULL,'/img/products/polo-rayas/blanco.jpg',3),(12,4,NULL,'/img/products/polo-rayas/gris.jpg',3),(13,5,NULL,'/img/products/polo-rayas/azul-marino.jpg',3),(14,3,NULL,'/img/products/camisa-oxford/blanco.jpg',4),(15,4,NULL,'/img/products/camisa-oxford/gris.jpg',4),(16,5,NULL,'/img/products/camisa-oxford/azul-marino.jpg',4),(17,1,NULL,'/img/products/camisa-denim/negro.jpg',5),(18,5,NULL,'/img/products/camisa-denim/azul-marino.jpg',5),(20,1,NULL,'/img/products/chino-khaki/negro.jpg',6),(21,5,NULL,'/img/products/chino-khaki/azul-marino.jpg',6),(22,7,NULL,'/img/products/chino-khaki/khaki.jpg',6),(23,1,NULL,'/img/products/jogger-negro/negro.jpg',7),(24,4,NULL,'/img/products/jogger-negro/gris.jpg',7),(26,1,NULL,'/img/products/bomber-olivo/negro.jpg',8),(27,6,NULL,'/img/products/bomber-olivo/verde-olivo.jpg',8),(28,7,NULL,'/img/products/bomber-olivo/khaki.jpg',8),(29,1,NULL,'/img/products/chaqueta-jean/negro.jpg',9),(30,5,NULL,'/img/products/chaqueta-jean/azul-marino.jpg',9),(32,3,NULL,'/img/products/vestido-floral/blanco.jpg',10),(33,8,NULL,'/img/products/vestido-floral/beige.jpg',10),(34,9,NULL,'/img/products/vestido-floral/rosa.jpg',10),(35,1,NULL,'/img/products/vestido-negro/negro.jpg',11),(36,9,NULL,'/img/products/vestido-negro/rosa.jpg',11),(38,1,NULL,'/img/products/zapatillas-urbanas/negro.jpg',12),(39,3,NULL,'/img/products/zapatillas-urbanas/blanco.jpg',12),(40,4,NULL,'/img/products/zapatillas-urbanas/gris.jpg',12),(41,1,NULL,'/img/products/running-pro/negro.jpg',13),(42,2,NULL,'/img/products/running-pro/rojo.jpg',13),(43,5,NULL,'/img/products/running-pro/azul-marino.jpg',13),(44,1,NULL,'/img/products/hoodie-gris/negro.jpg',14),(45,4,NULL,'/img/products/hoodie-gris/gris.jpg',14),(46,5,NULL,'/img/products/hoodie-gris/azul-marino.jpg',14),(47,1,NULL,'/img/products/sudadera-zip/negro.jpg',15),(48,5,NULL,'/img/products/sudadera-zip/azul-marino.jpg',15),(50,1,NULL,'/img/products/falda-plisada/negro.jpg',16),(51,8,NULL,'/img/products/falda-plisada/beige.jpg',16),(52,9,NULL,'/img/products/falda-plisada/rosa.jpg',16),(53,3,NULL,'/img/products/blusa-seda/blanco.jpg',17),(54,8,NULL,'/img/products/blusa-seda/beige.jpg',17),(55,9,NULL,'/img/products/blusa-seda/rosa.jpg',17),(56,1,NULL,'/img/products/short-deportivo/negro.jpg',18),(57,4,NULL,'/img/products/short-deportivo/gris.jpg',18),(58,5,NULL,'/img/products/short-deportivo/azul-marino.jpg',18),(59,1,NULL,'/img/products/polera-oversize/negro.jpg',19),(60,4,NULL,'/img/products/polera-oversize/gris.jpg',19),(61,8,NULL,'/img/products/polera-oversize/beige.jpg',19),(62,1,NULL,'/img/products/cardigan-tejido/negro.jpg',20),(63,7,NULL,'/img/products/cardigan-tejido/khaki.jpg',20),(64,8,NULL,'/img/products/cardigan-tejido/beige.jpg',20),(65,1,NULL,'/img/products/camiseta-basica-pack/negro.jpg',21),(66,3,NULL,'/img/products/camiseta-basica-pack/blanco.jpg',21),(67,4,NULL,'/img/products/camiseta-basica-pack/gris.jpg',21);
/*!40000 ALTER TABLE `imagenes_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_carrito`
--

DROP TABLE IF EXISTS `item_carrito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_carrito` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_variacion_producto` bigint unsigned NOT NULL,
  `cantidad` bigint NOT NULL,
  `id_carrito` bigint unsigned NOT NULL,
  `precio_unitario_guardado` decimal(8,2) NOT NULL,
  `descuento_aplicado` decimal(8,2) NOT NULL,
  `id_promocion_aplicada` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `item_carrito_id_promocion_aplicada_foreign` (`id_promocion_aplicada`),
  KEY `item_carrito_id_carrito_foreign` (`id_carrito`),
  KEY `item_carrito_id_variacion_producto_foreign` (`id_variacion_producto`),
  CONSTRAINT `item_carrito_id_carrito_foreign` FOREIGN KEY (`id_carrito`) REFERENCES `carrito` (`id`),
  CONSTRAINT `item_carrito_id_promocion_aplicada_foreign` FOREIGN KEY (`id_promocion_aplicada`) REFERENCES `promocion` (`id`),
  CONSTRAINT `item_carrito_id_variacion_producto_foreign` FOREIGN KEY (`id_variacion_producto`) REFERENCES `variaciones_producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_carrito`
--

LOCK TABLES `item_carrito` WRITE;
/*!40000 ALTER TABLE `item_carrito` DISABLE KEYS */;
/*!40000 ALTER TABLE `item_carrito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `size_id` bigint DEFAULT NULL,
  `color_id` bigint DEFAULT NULL,
  `qty` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `image` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_items_order` (`order_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,2,19,5,8,1,119.90,119.90,'Polera Oversize Arena','/img/products/polera-oversize/preview.jpg'),(2,3,19,5,8,1,119.90,119.90,'Polera Oversize Arena','/img/products/polera-oversize/preview.jpg'),(3,4,16,5,8,1,129.90,129.90,'Falda Plisada Beige','/img/products/falda-plisada/preview.jpg'),(4,5,20,5,8,1,179.90,179.90,'Cardigan Tejido','/img/products/cardigan-tejido/preview.jpg');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(32) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `igv` decimal(10,2) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `created_at` datetime NOT NULL,
  `status` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_number` (`order_number`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'TESTORDER123','test@example.com',10.00,1.80,11.80,'2025-11-22 03:10:39',NULL),(2,'SF1763799527','rodolfo.tavera@tecsup.edu.pe',119.90,21.58,141.48,'2025-11-22 03:19:05',NULL),(3,'SF1763799897','rodolfo.tavera@tecsup.edu.pe',119.90,21.58,141.48,'2025-11-22 03:25:27',NULL),(4,'SF1763799981','rodolfo.tavera@tecsup.edu.pe',129.90,23.38,153.28,'2025-11-22 03:26:44',NULL),(5,'SF1763804116','rodolfo.tavera@tecsup.edu.pe',179.90,32.38,212.28,'2025-11-22 04:35:35',NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `fecha_pedido` bigint NOT NULL,
  `estado` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'El estado de la orden (Ej: ''Pagado'', ''Procesando Env├¡o'', ''Entregado'', ''Cancelado'').',
  `total_neto_productos` decimal(8,2) NOT NULL COMMENT 'El valor de los productos despu├®s de todos los descuentos.',
  `costo_envio` decimal(8,2) NOT NULL,
  `total_final` decimal(8,2) NOT NULL,
  `direccion_envio` bigint NOT NULL,
  `id_carrito` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `pedido_id_carrito_foreign` (`id_carrito`),
  KEY `pedido_id_usuario_foreign` (`id_usuario`),
  CONSTRAINT `pedido_id_carrito_foreign` FOREIGN KEY (`id_carrito`) REFERENCES `carrito` (`id`),
  CONSTRAINT `pedido_id_usuario_foreign` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido`
--

LOCK TABLES `pedido` WRITE;
/*!40000 ALTER TABLE `pedido` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_reviews`
--

DROP TABLE IF EXISTS `product_reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `user_email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rating` tinyint NOT NULL,
  `text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_reviews`
--

LOCK TABLES `product_reviews` WRITE;
/*!40000 ALTER TABLE `product_reviews` DISABLE KEYS */;
INSERT INTO `product_reviews` VALUES (1,1,'yasstavera@gmail.com',3,'Hola','2025-11-12 17:39:47');
/*!40000 ALTER TABLE `product_reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `precio` decimal(8,2) NOT NULL,
  `id_categoria` bigint unsigned NOT NULL,
  `image_preview` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `producto_id_categoria_foreign` (`id_categoria`),
  CONSTRAINT `producto_id_categoria_foreign` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,'Polo','Un polo bonito',12.00,1,'https://rematexperu.com/cdn/shop/products/30_313b4046-564b-4ed0-81ff-36a14415821c_800x.png?v=1668045565'),(2,'Polo Classic','Polo de algod├│n premium, corte cl├ísico y respirable.',59.90,2,'/img/products/polo-classic/preview.jpg'),(3,'Polo Rayas Marinas','Polo de rayas inspirado en el estilo n├íutico.',64.90,2,'/img/products/polo-rayas/preview.jpg'),(4,'Camisa Oxford Blanca','Camisa oxford 100% algod├│n, ideal para oficina y eventos.',109.90,3,'/img/products/camisa-oxford/preview.jpg'),(5,'Camisa Denim Azul','Camisa denim resistente con lavado medio.',129.90,3,'/img/products/camisa-denim/preview.jpg'),(6,'Pantal├│n Chino Khaki','Chino slim fit, c├│modo y vers├ítil.',139.90,4,'/img/products/chino-khaki/preview.jpg'),(7,'Pantal├│n Jogger Negro','Jogger con pu├▒o el├ístico y tela ligera.',119.90,4,'/img/products/jogger-negro/preview.jpg'),(8,'Chaqueta Bomber Olivo','Bomber ligera con bolsillos y forro suave.',199.90,5,'/img/products/bomber-olivo/preview.jpg'),(9,'Chaqueta Jean Cl├ísica','Chaqueta de jean con lavado tradicional.',189.90,5,'/img/products/chaqueta-jean/preview.jpg'),(10,'Vestido Floral Midi','Vestido midi estampado floral, tela vaporosa.',169.90,6,'/img/products/vestido-floral/preview.jpg'),(11,'Vestido Negro Elegante','Corte entallado y tela el├ística de alta calidad.',199.90,6,'/img/products/vestido-negro/preview.jpg'),(12,'Zapatillas Urbanas','Zapatillas para uso diario con suela liviana.',229.90,7,'/img/products/zapatillas-urbanas/preview.jpg'),(13,'Zapatillas Running Pro','Amortiguaci├│n avanzada y malla respirable.',289.90,7,'/img/products/running-pro/preview.jpg'),(14,'Sudadera Hoodie Gris','Hoodie suave con bolsillos tipo canguro.',149.90,8,'/img/products/hoodie-gris/preview.jpg'),(15,'Sudadera Zip Azul Marino','Cierre completo, interior afelpado.',159.90,8,'/img/products/sudadera-zip/preview.jpg'),(16,'Falda Plisada Beige','Falda midi plisada con ca├¡da fluida.',129.90,9,'/img/products/falda-plisada/preview.jpg'),(17,'Blusa Seda Crema','Blusa de seda ligera con ca├¡da natural.',159.90,10,'/img/products/blusa-seda/preview.jpg'),(18,'Short Deportivo','Short ligero de poli├®ster con secado r├ípido.',89.90,11,'/img/products/short-deportivo/preview.jpg'),(19,'Polera Oversize Arena','Polera corte oversize, algod├│n pesado.',119.90,12,'/img/products/polera-oversize/preview.jpg'),(20,'Cardigan Tejido','Cardigan c├ílido con punto medio y botones.',179.90,13,'/img/products/cardigan-tejido/preview.jpg'),(21,'Camiseta B├ísica Pack','Pack de 2 camisetas b├ísicas, algod├│n suave.',99.90,14,'/img/products/camiseta-basica-pack/preview.jpg');
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promocion`
--

DROP TABLE IF EXISTS `promocion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promocion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo_descuento` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Ej: ''PORCENTAJE'', ''MONTO_FIJO''',
  `valor` decimal(8,2) NOT NULL COMMENT 'Ej: 0.15 o 10.00',
  `fecha_inicio` timestamp NOT NULL,
  `fecha_fin` timestamp NOT NULL,
  `activo` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promocion`
--

LOCK TABLES `promocion` WRITE;
/*!40000 ALTER TABLE `promocion` DISABLE KEYS */;
INSERT INTO `promocion` VALUES (1,'PRIV11','Primavera','MONTO_FIJO',10.00,'2025-11-11 22:19:00','2025-11-15 22:19:00',1);
/*!40000 ALTER TABLE `promocion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reclamacion`
--

DROP TABLE IF EXISTS `reclamacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reclamacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(64) NOT NULL,
  `email` varchar(255) NOT NULL,
  `telefono` varchar(64) DEFAULT NULL,
  `tipo` varchar(16) NOT NULL,
  `detalle` text NOT NULL,
  `estado` varchar(32) NOT NULL DEFAULT 'registrado',
  `respuesta` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reclamacion`
--

LOCK TABLES `reclamacion` WRITE;
/*!40000 ALTER TABLE `reclamacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `reclamacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reglaenvio`
--

DROP TABLE IF EXISTS `reglaenvio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reglaenvio` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `origen_region` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `destino_region` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_empresa_envio` bigint unsigned NOT NULL,
  `prioridad` int NOT NULL DEFAULT '1',
  `costo` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `regla_envio_empresa_fk` (`id_empresa_envio`),
  CONSTRAINT `regla_envio_empresa_fk` FOREIGN KEY (`id_empresa_envio`) REFERENCES `empresaenvio` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reglaenvio`
--

LOCK TABLES `reglaenvio` WRITE;
/*!40000 ALTER TABLE `reglaenvio` DISABLE KEYS */;
INSERT INTO `reglaenvio` VALUES (1,'Lima','Lima',1,1,12.00);
/*!40000 ALTER TABLE `reglaenvio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_requests`
--

DROP TABLE IF EXISTS `return_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_email` varchar(255) NOT NULL,
  `order_number` varchar(64) NOT NULL,
  `motivo` varchar(64) NOT NULL,
  `descripcion` text,
  `metodo` varchar(16) NOT NULL,
  `estado` varchar(32) NOT NULL DEFAULT 'solicitado',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_returns_email` (`user_email`),
  KEY `idx_returns_order` (`order_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_requests`
--

LOCK TABLES `return_requests` WRITE;
/*!40000 ALTER TABLE `return_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `return_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tallas`
--

DROP TABLE IF EXISTS `tallas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tallas` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tallas`
--

LOCK TABLES `tallas` WRITE;
/*!40000 ALTER TABLE `tallas` DISABLE KEYS */;
INSERT INTO `tallas` VALUES (1,'L','Ropa'),(2,'XL','Ropa'),(3,'XS','ropa'),(4,'S','ropa'),(5,'M','ropa'),(6,'38','calzado'),(7,'39','calzado'),(8,'40','calzado'),(9,'41','calzado'),(10,'42','calzado');
/*!40000 ALTER TABLE `tallas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaccion_pago`
--

DROP TABLE IF EXISTS `transaccion_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaccion_pago` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_pedido` bigint unsigned NOT NULL,
  `monto` decimal(8,2) NOT NULL COMMENT 'El monto exacto que se proces├│ (debe coincidir con Pedido.total_final).',
  `fecha_transaccion` timestamp NOT NULL,
  `metodo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'El m├®todo usado (Ej: ''Tarjeta de Cr├®dito'', ''PayPal'', ''Transferencia'').',
  `pasarela_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'CR├ìTICO: El ID ├║nico que la pasarela de pago te devuelve (Ej: ch_1FzS...).',
  `estado_pago` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'El estado final (Ej: ''Aprobado'', ''Rechazado'', ''Pendiente'').',
  PRIMARY KEY (`id`),
  KEY `transaccion_pago_id_pedido_foreign` (`id_pedido`),
  CONSTRAINT `transaccion_pago_id_pedido_foreign` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaccion_pago`
--

LOCK TABLES `transaccion_pago` WRITE;
/*!40000 ALTER TABLE `transaccion_pago` DISABLE KEYS */;
/*!40000 ALTER TABLE `transaccion_pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_email` varchar(255) NOT NULL,
  `label` varchar(120) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `alt_telefono` varchar(50) DEFAULT NULL,
  `direccion` varchar(512) NOT NULL,
  `direccion_linea2` varchar(512) DEFAULT NULL,
  `distrito` varchar(255) DEFAULT NULL,
  `ciudad` varchar(255) DEFAULT NULL,
  `region` varchar(255) NOT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `pais` varchar(100) DEFAULT NULL,
  `codigo_postal` varchar(32) DEFAULT NULL,
  `referencia` varchar(512) DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_address_user` (`user_email`),
  KEY `idx_user_address_default` (`user_email`,`is_default`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
INSERT INTO `user_address` VALUES (1,'rodolfo.tavera@tecsup.edu.pe','I don\'t know','Rodolfo Tavera','917364262','917364263','Avenida America','Trujillo xd','Trujillo','Trujillo','Otro','Trujillo',NULL,'000','Por mi casa',1,'2025-11-18 11:31:00','2025-11-22 19:12:57');
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_emails`
--

DROP TABLE IF EXISTS `user_emails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_emails` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_verified` tinyint(1) DEFAULT '0',
  `is_primary` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_email` (`user_id`,`email`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_emails`
--

LOCK TABLES `user_emails` WRITE;
/*!40000 ALTER TABLE `user_emails` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_emails` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_security`
--

DROP TABLE IF EXISTS `user_security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_security` (
  `user_id` bigint NOT NULL,
  `totp_secret` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `totp_enabled` tinyint(1) DEFAULT '0',
  `revoked_after` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_security`
--

LOCK TABLES `user_security` WRITE;
/*!40000 ALTER TABLE `user_security` DISABLE KEYS */;
INSERT INTO `user_security` VALUES (1,'II36C2QMEALUQBQBZPI2V4HNBEAEKK5P',1,NULL);
/*!40000 ALTER TABLE `user_security` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_sessions`
--

DROP TABLE IF EXISTS `user_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `jti` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_agent` text COLLATE utf8mb4_unicode_ci,
  `ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_sessions`
--

LOCK TABLES `user_sessions` WRITE;
/*!40000 ALTER TABLE `user_sessions` DISABLE KEYS */;
INSERT INTO `user_sessions` VALUES (1,2,'255bf3b139454356830ba9318694fbe2','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36','127.0.0.1','2025-11-18 16:12:57');
/*!40000 ALTER TABLE `user_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellido` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_registro` bigint NOT NULL,
  `bloqueado` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'','','yasstavera@gmail.com','',1762986187,0),(2,'','','rodolfo.tavera@tecsup.edu.pe','',1763500359,0);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `variaciones_producto`
--

DROP TABLE IF EXISTS `variaciones_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variaciones_producto` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_producto` bigint unsigned NOT NULL,
  `id_talla` bigint unsigned NOT NULL,
  `id_color` bigint unsigned NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_variacion` (`id_producto`,`id_talla`,`id_color`),
  KEY `variaciones_producto_id_talla_foreign` (`id_talla`),
  CONSTRAINT `variaciones_producto_id_producto_foreign` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`),
  CONSTRAINT `variaciones_producto_id_talla_foreign` FOREIGN KEY (`id_talla`) REFERENCES `tallas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=320 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `variaciones_producto`
--

LOCK TABLES `variaciones_producto` WRITE;
/*!40000 ALTER TABLE `variaciones_producto` DISABLE KEYS */;
INSERT INTO `variaciones_producto` VALUES (1,1,1,1,24),(3,1,2,2,30),(4,2,5,1,17),(5,2,4,1,17),(6,2,3,1,25),(7,2,2,1,20),(8,2,1,1,17),(9,2,5,3,16),(10,2,4,3,18),(11,2,3,3,11),(12,2,2,3,14),(13,2,1,3,26),(14,2,5,4,15),(15,2,4,4,29),(16,2,3,4,30),(17,2,2,4,12),(18,2,1,4,24),(19,2,5,5,29),(20,2,4,5,25),(21,2,3,5,25),(22,2,2,5,22),(23,2,1,5,25),(35,3,5,3,29),(36,3,4,3,20),(37,3,3,3,24),(38,3,2,3,28),(39,3,1,3,16),(40,3,5,4,28),(41,3,4,4,18),(42,3,3,4,21),(43,3,2,4,19),(44,3,1,4,22),(45,3,5,5,25),(46,3,4,5,25),(47,3,3,5,21),(48,3,2,5,19),(49,3,1,5,25),(50,4,5,3,13),(51,4,4,3,26),(52,4,2,3,18),(53,4,1,3,21),(54,4,5,4,24),(55,4,4,4,23),(56,4,2,4,14),(57,4,1,4,12),(58,4,5,5,11),(59,4,4,5,28),(60,4,2,5,15),(61,4,1,5,21),(65,5,5,1,11),(66,5,4,1,24),(67,5,2,1,13),(68,5,1,1,27),(69,5,5,5,22),(70,5,4,5,18),(71,5,2,5,18),(72,5,1,5,24),(80,6,5,1,17),(81,6,4,1,24),(82,6,2,1,17),(83,6,1,1,26),(84,6,5,5,26),(85,6,4,5,22),(86,6,2,5,24),(87,6,1,5,23),(88,6,5,7,13),(89,6,4,7,27),(90,6,2,7,23),(91,6,1,7,24),(95,7,5,1,20),(96,7,4,1,18),(97,7,2,1,22),(98,7,1,1,24),(99,7,5,4,25),(100,7,4,4,20),(101,7,2,4,17),(102,7,1,4,17),(110,8,5,1,23),(111,8,4,1,11),(112,8,2,1,19),(113,8,1,1,11),(114,8,5,6,10),(115,8,4,6,28),(116,8,2,6,17),(117,8,1,6,13),(118,8,5,7,27),(119,8,4,7,20),(120,8,2,7,13),(121,8,1,7,13),(125,9,5,1,18),(126,9,4,1,19),(127,9,2,1,13),(128,9,1,1,21),(129,9,5,5,11),(130,9,4,5,26),(131,9,2,5,24),(132,9,1,5,13),(140,10,5,3,25),(141,10,4,3,14),(142,10,3,3,28),(143,10,1,3,23),(144,10,5,8,24),(145,10,4,8,22),(146,10,3,8,25),(147,10,1,8,10),(148,10,5,9,27),(149,10,4,9,14),(150,10,3,9,19),(151,10,1,9,22),(155,11,1,1,23),(156,11,2,1,20),(157,11,3,1,20),(158,11,4,1,29),(159,11,5,1,14),(160,11,1,9,16),(161,11,2,9,28),(162,11,3,9,19),(163,11,4,9,21),(164,11,5,9,20),(170,12,6,1,27),(171,12,7,1,22),(172,12,8,1,19),(173,12,9,1,22),(174,12,10,1,20),(175,12,6,3,27),(176,12,7,3,21),(177,12,8,3,17),(178,12,9,3,30),(179,12,10,3,30),(180,12,6,4,26),(181,12,7,4,13),(182,12,8,4,19),(183,12,9,4,23),(184,12,10,4,28),(185,13,6,1,22),(186,13,7,1,13),(187,13,8,1,14),(188,13,9,1,18),(189,13,10,1,19),(190,13,6,2,10),(191,13,7,2,27),(192,13,8,2,13),(193,13,9,2,13),(194,13,10,2,19),(195,13,6,5,25),(196,13,7,5,15),(197,13,8,5,13),(198,13,9,5,30),(199,13,10,5,17),(200,14,1,1,29),(201,14,2,1,21),(202,14,4,1,10),(203,14,5,1,19),(204,14,1,4,14),(205,14,2,4,24),(206,14,4,4,27),(207,14,5,4,30),(208,14,1,5,20),(209,14,2,5,20),(210,14,4,5,30),(211,14,5,5,20),(215,15,1,1,21),(216,15,2,1,13),(217,15,4,1,12),(218,15,5,1,14),(219,15,1,5,23),(220,15,2,5,23),(221,15,4,5,19),(222,15,5,5,15),(230,16,1,1,30),(231,16,3,1,10),(232,16,4,1,16),(233,16,5,1,17),(234,16,1,8,29),(235,16,3,8,23),(236,16,4,8,17),(237,16,5,8,25),(238,16,1,9,27),(239,16,3,9,26),(240,16,4,9,19),(241,16,5,9,29),(245,17,1,3,18),(246,17,3,3,15),(247,17,4,3,29),(248,17,5,3,10),(249,17,1,8,13),(250,17,3,8,26),(251,17,4,8,21),(252,17,5,8,16),(253,17,1,9,30),(254,17,3,9,10),(255,17,4,9,10),(256,17,5,9,12),(260,18,1,1,21),(261,18,2,1,17),(262,18,4,1,14),(263,18,5,1,29),(264,18,1,4,30),(265,18,2,4,14),(266,18,4,4,11),(267,18,5,4,23),(268,18,1,5,12),(269,18,2,5,23),(270,18,4,5,28),(271,18,5,5,21),(275,19,1,1,10),(276,19,2,1,20),(277,19,4,1,19),(278,19,5,1,25),(279,19,1,4,15),(280,19,2,4,13),(281,19,4,4,12),(282,19,5,4,10),(283,19,1,8,26),(284,19,2,8,29),(285,19,4,8,16),(286,19,5,8,19),(290,20,1,1,23),(291,20,2,1,30),(292,20,4,1,10),(293,20,5,1,12),(294,20,1,7,20),(295,20,2,7,14),(296,20,4,7,19),(297,20,5,7,25),(298,20,1,8,15),(299,20,2,8,14),(300,20,4,8,14),(301,20,5,8,20),(305,21,1,1,29),(306,21,2,1,10),(307,21,3,1,16),(308,21,4,1,21),(309,21,5,1,26),(310,21,1,3,16),(311,21,2,3,11),(312,21,3,3,21),(313,21,4,3,19),(314,21,5,3,25),(315,21,1,4,15),(316,21,2,4,12),(317,21,3,4,25),(318,21,4,4,16),(319,21,5,4,19);
/*!40000 ALTER TABLE `variaciones_producto` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-23 17:35:29
