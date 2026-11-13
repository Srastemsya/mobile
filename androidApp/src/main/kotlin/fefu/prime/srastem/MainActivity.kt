package fefu.prime.srastem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val state = ViewModelProvider(this)[ActivityStateHolder::class.java]
        intent.dataString?.let { link ->
            // Deeplink-приглашение комнаты (FE-F04): экран комнаты открывается поверх ленты.
            state.main.navigateTo(Destination.FeedScreen)
            state.feed.openRoomFromInvite()
            state.roomLobby.acceptInvite(link)
        }
        setContent {
            NavigationMain(state.main, state.onboarding, state.registration, state.party, state.feed, state.roomLobby, state.profile, state.events, state.home, state.map)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let { link ->
            val state = ViewModelProvider(this)[ActivityStateHolder::class.java]
            state.main.navigateTo(Destination.FeedScreen)
            state.feed.openRoomFromInvite()
            state.roomLobby.acceptInvite(link)
        }
    }
}
