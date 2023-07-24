package com.ebata_shota.baroalitimeter.ui.parts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ClickableCard(
    text: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(200.dp),
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = modifier.padding(16.dp),
                fontSize = 40.sp,
                text = text
            )
        }
    }
}

@Preview
@Composable
fun EnableViewerModeContentPreview() {
    BaroAlitimeterTheme {
        ClickableCard(
            text = "テキスト",
            onClick = {}
        )
    }
}

@Preview
@Composable
fun NotEnableViewerModeContentPreview() {
    BaroAlitimeterTheme {
        ClickableCard(
            text = "1000",
            onClick = null
        )
    }
}
