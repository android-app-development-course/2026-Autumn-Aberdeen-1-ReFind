package com.refind.app.data

/**
 * Demo data for the phase-2 UI prototype. Everything is in-memory and
 * hand-written to tell one coherent campus story: Peter loses his water
 * bottle, ReFind matches it with a finder's report, they verify and chat.
 */

enum class ItemStatus { LOST, FOUND }

enum class Category(val label: String) {
    PHONE("Phone"),
    WALLET("Wallet"),
    KEYS("Keys"),
    HEADPHONES("Headphones"),
    BOTTLE("Bottle"),
    BOOK("Book"),
    BAG("Bag"),
    OTHER("Other"),
}

data class ItemReport(
    val id: Int,
    val title: String,
    val category: Category,
    val location: String,
    val timeAgo: String,
    val status: ItemStatus,
    val colour: String,
    val reward: Int? = null,
    val reporter: String,
)

data class ChatMessage(
    val id: Int,
    val text: String,
    val isMe: Boolean,
    val time: String,
    val isSystem: Boolean = false,
)

object DemoData {
    const val USER_NAME = "Peter"

    val lostItems = listOf(
        ItemReport(
            id = 1,
            title = "Blue water bottle",
            category = Category.BOTTLE,
            location = "Main Library, 2F",
            timeAgo = "2h ago",
            status = ItemStatus.LOST,
            colour = "Blue",
            reward = 5,
            reporter = USER_NAME,
        ),
        ItemReport(
            id = 2,
            title = "Black leather wallet",
            category = Category.WALLET,
            location = "Sports Centre",
            timeAgo = "5h ago",
            status = ItemStatus.LOST,
            colour = "Black",
            reward = 20,
            reporter = USER_NAME,
        ),
        ItemReport(
            id = 3,
            title = "Car keys, panda charm",
            category = Category.KEYS,
            location = "Parking Lot B",
            timeAgo = "Yesterday",
            status = ItemStatus.LOST,
            colour = "Silver",
            reporter = USER_NAME,
        ),
        ItemReport(
            id = 4,
            title = "Silver watch",
            category = Category.OTHER,
            location = "University Gym",
            timeAgo = "2 days ago",
            status = ItemStatus.LOST,
            colour = "Silver",
            reporter = USER_NAME,
        ),
    )

    val foundItems = listOf(
        ItemReport(
            id = 11,
            title = "Water bottle, blue",
            category = Category.BOTTLE,
            location = "Library Cafe",
            timeAgo = "1h ago",
            status = ItemStatus.FOUND,
            colour = "Blue",
            reporter = "Amy",
        ),
        ItemReport(
            id = 12,
            title = "AirPods case",
            category = Category.HEADPHONES,
            location = "Hall C lounge",
            timeAgo = "1h ago",
            status = ItemStatus.FOUND,
            colour = "White",
            reporter = "Ben",
        ),
        ItemReport(
            id = 13,
            title = "Physics textbook, 3rd ed.",
            category = Category.BOOK,
            location = "Cafeteria",
            timeAgo = "3h ago",
            status = ItemStatus.FOUND,
            colour = "Orange",
            reporter = "Priya",
        ),
        ItemReport(
            id = 14,
            title = "Transparent umbrella",
            category = Category.OTHER,
            location = "Main Gate",
            timeAgo = "Yesterday",
            status = ItemStatus.FOUND,
            colour = "Clear",
            reporter = "Amy",
        ),
    )

    // The story match: Peter's lost bottle vs Amy's find near the library.
    const val MATCHED_LOST_ID = 1
    const val MATCHED_FOUND_ID = 11
    const val MATCH_CONFIDENCE = 0.92f

    val chatMessages = listOf(
        ChatMessage(
            id = 0,
            text = "Match confirmed with 92% confidence. Please verify ownership before handover.",
            isMe = false,
            time = "09:41",
            isSystem = true,
        ),
        ChatMessage(
            id = 1,
            text = "Hi! I found a blue bottle at the Library Cafe about an hour ago 👋",
            isMe = false,
            time = "09:42",
        ),
        ChatMessage(
            id = 2,
            text = "That's probably mine! Lost it somewhere on 2F this morning.",
            isMe = true,
            time = "09:43",
        ),
        ChatMessage(
            id = 3,
            text = "Verification question: what sticker is on the bottle?",
            isMe = false,
            time = "09:44",
        ),
        ChatMessage(
            id = 4,
            text = "A white Miffy sticker right under the cap 😊",
            isMe = true,
            time = "09:45",
        ),
        ChatMessage(
            id = 5,
            text = "That's the one! Meet at the Library Cafe safe point at 3pm?",
            isMe = false,
            time = "09:46",
        ),
    )
}
