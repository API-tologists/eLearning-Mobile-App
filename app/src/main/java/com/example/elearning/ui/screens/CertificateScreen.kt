package com.example.elearning.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.elearning.model.Certificate
import com.example.elearning.util.CertificatePdfGenerator
import com.example.elearning.viewmodel.CourseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    navController: NavController,
    courseViewModel: CourseViewModel,
    certificateId: String
) {
    val certificate by courseViewModel.certificate.collectAsState()
    val context = LocalContext.current
    var showDownloading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(certificateId) {
        courseViewModel.getCertificate(certificateId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificate of Completion") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    certificate?.let {
                        IconButton(
                            onClick = {
                                showDownloading = true
                                scope.launch {
                                    val pdfGenerator = CertificatePdfGenerator(context)
                                    val pdfFile = pdfGenerator.generatePdf(it)
                                    showDownloading = false
                                    
                                    pdfFile?.let { file ->
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Download Certificate")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (showDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                certificate?.let { cert ->
                    CertificateContent(certificate = cert)
                }
            }
        }
    }
}

@Composable
private fun CertificateContent(certificate: Certificate) {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(certificate.issueDate))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Certificate Header
            Text(
                text = "Certificate of Completion",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Certificate Number
            Text(
                text = "Certificate No: ${certificate.certificateNumber}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Content
            Text(
                text = "This is to certify that",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = certificate.studentName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "has successfully completed the course",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = certificate.courseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instructor and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Instructor",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = certificate.instructorName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Verification Note
            Text(
                text = "This certificate can be verified using the certificate number",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 