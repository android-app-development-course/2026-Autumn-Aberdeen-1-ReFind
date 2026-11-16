package com.refind.app.data

/**
 * Demo data for the phase-2 UI prototype. Everything is in-memory and
 * hand-written to tell one coherent campus story: Peter loses his water
 * bottle, ReFind matches it with a finder's report, they verify and chat.
 *
 * The feed mixes reports from the whole community (Peter's own entries are
 * flagged "YOU" in the UI); the profile lists only Peter's own reports.
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

    /** Community lost reports — Peter's own two plus other students'. */
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
            title = "Pink umbrella",
            category = Category.OTHER,
            location = "West Hall entrance",
            timeAgo = "Yesterday",
            status = ItemStatus.LOST,
            colour = "Pink",
            reporter = "Sara",
        ),
        ItemReport(
            id = 4,
            title = "Laptop bag, grey",
            category = Category.BAG,
            location = "Lecture Hall B",
            timeAgo = "2 days ago",
            status = ItemStatus.LOST,
            colour = "Grey",
            reporter = "Tom",
        ),
    )

    /** Community found reports from other students. */
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
            title = "Set of keys with strap",
            category = Category.KEYS,
            location = "Main Gate",
            timeAgo = "Yesterday",
            status = ItemStatus.FOUND,
            colour = "Silver",
            reporter = "Ben",
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

    /** Chat threads shown on the Messages inbox screen. */
    val conversations = listOf(
        Conversation(
            id = 1,
            peerName = "Amy",
            peerRole = "Finder · Library Cafe",
            peerOnline = true,
            item = foundItems.first { it.id == MATCHED_FOUND_ID },
            messages = chatMessages,
            preview = "That's the one! Meet at the Library Cafe safe point at 3pm?",
            time = "09:46",
            unread = false,
        ),
        Conversation(
            id = 2,
            peerName = "ReFind Team",
            peerRole = "Automated matching assistant",
            peerOnline = false,
            item = lostItems.first { it.id == 2 },
            messages = listOf(
                ChatMessage(
                    id = 0,
                    text = "Good news! Your lost wallet received a new potential match (87%). Review it from the home banner or the report page.",
                    isMe = false,
                    time = "08:30",
                ),
            ),
            preview = "Good news! Your lost wallet received a new potential match…",
            time = "08:30",
            unread = true,
        ),
    )
}

/** A chat thread shown on the Messages inbox screen. */
data class Conversation(
    val id: Int,
    val peerName: String,
    val peerRole: String,
    val peerOnline: Boolean,
    val item: ItemReport?,
    val messages: List<ChatMessage>,
    val preview: String,
    val time: String,
    val unread: Boolean,
)
