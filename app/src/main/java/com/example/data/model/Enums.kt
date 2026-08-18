package com.example.data.model

enum class ReturnStatus(val displayName: String) {
    UNKNOWN("Unknown"),
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    RETURNED("Returned"),
    NON_RETURNABLE("Non-returnable")
}

enum class WarrantyStatus(val displayName: String) {
    UNKNOWN("Unknown"),
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    CLAIMED("Claimed")
}

enum class DocumentType(val displayName: String) {
    RECEIPT("Receipt"),
    INVOICE("Invoice"),
    WARRANTY_CARD("Warranty Card"),
    MANUAL("Manual"),
    PRODUCT_PHOTO("Product Photo"),
    SERIAL_PHOTO("Serial Photo"),
    OTHER("Other")
}

enum class PurchaseCategory(val displayName: String, val iconName: String) {
    ELECTRONICS("Electronics", "devices"),
    PHONES("Phones", "smartphone"),
    COMPUTERS("Computers", "laptop"),
    HOME_APPLIANCES("Home Appliances", "kitchen"),
    FURNITURE("Furniture", "chair"),
    CLOTHING("Clothing", "checkroom"),
    SHOES("Shoes", "steps"),
    TOOLS("Tools", "build"),
    VEHICLES("Vehicles", "directions_car"),
    VEHICLE_PARTS("Vehicle Parts", "settings"),
    GAMING("Gaming", "sports_esports"),
    SUBSCRIPTIONS("Subscriptions", "autorenew"),
    SOFTWARE("Software", "code"),
    FOOD("Food & Dining", "restaurant"),
    HEALTH_PERSONAL_CARE("Health & Care", "favorite"),
    BEAUTY("Beauty", "face"),
    CHILDREN("Children", "child_care"),
    PET("Pet", "pets"),
    HOME("Home", "home"),
    OFFICE("Office", "work"),
    OTHER("Other", "category");

    companion object {
        fun fromString(value: String): PurchaseCategory {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
            } ?: OTHER
        }
    }
}
