package com.ebata_shota.baroalitimeter.ui.parts

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ebata_shota.baroalitimeter.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainTopAppBar(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    showTopAppBarDropdownMenu: () -> Unit,
    hideTopAppBarDropdownMenu: () -> Unit,
    onClickTheme: () -> Unit,
) {
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        TopAppBar(
            title = {
                Text(stringResource(id = R.string.title))
            },
            actions = {
                IconButton(
                    onClick = showTopAppBarDropdownMenu
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null
                    )
                    DropdownMenu(
                        modifier = modifier.padding(horizontal = 4.dp),
                        expanded = expanded,
                        onDismissRequest = hideTopAppBarDropdownMenu
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_palette_24),
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.theme))
                            },
                            onClick = {
                                hideTopAppBarDropdownMenu()
                                onClickTheme()
                            }
                        )
                        Divider()
                        val context = LocalContext.current
                        DropdownMenuItem(
                            text = {
                                Text(text = stringResource(id = R.string.oss))
                            },
                            onClick = {
                                hideTopAppBarDropdownMenu()
                                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                            }
                        )
                    }
                }
            }
        )
    }
}