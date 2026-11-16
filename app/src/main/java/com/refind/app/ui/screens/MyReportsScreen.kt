package com.refind.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.refind.app.data.ItemReport
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.data.ItemStatus
import com.refind.app.ui.components.ItemCard
import com.refind.app.ui.components.StatusTabs

/**
 * Full list of the signed-in user's own reports, split by Lost / Found.
 * Reached from the stacked preview card on the profile.
 */
@Composable
fun MyReportsScreen(
    myReports: List<ItemReport>,
    onBack: () -> Unit,
    onNewReport: () -> Unit,
    onItemClick: (ItemReport) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(ItemStatus.LOST) }
    val items = myReports.filter { it.status == selectedTab }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = tr(R.string.my_reports_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNewReport) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "New report",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            StatusTabs(
                selected = selectedTab,
                lostCount = myReports.count { it.status == ItemStatus.LOST },
                foundCount = myReports.count { it.status == ItemStatus.FOUND },
                onSelect = { selectedTab = it },
            )
        }

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = tr(R.string.my_reports_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (selectedTab == ItemStatus.FOUND) {
                        tr(R.string.my_reports_empty_found)
                    } else {
                        tr(R.string.my_reports_empty_lost)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(260.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(items, key = { _, item -> "my-${item.id}" }) { index, item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        index = index,
                        isMine = true,
                    )
                }
            }
        }
    }
}
