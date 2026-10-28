import { DailyProgressReport, PaymentRecord, AttendanceRecord } from '../types/schema';

/**
 * Format and trigger WhatsApp share for a Daily Progress Report (DPR)
 */
export const shareDPRViaWhatsApp = (dpr: DailyProgressReport, phoneNumber?: string): void => {
  const text = `🏗️ *NIRMAAN SETU — DAILY PROGRESS REPORT*
📅 *Date:* ${dpr.reportDate}
🏢 *Project:* ${dpr.projectTitle}
📍 *Site:* ${dpr.siteName}
👷 *Labor on Site:* ${dpr.skilledLaborCount} Skilled, ${dpr.unskilledLaborCount} Helpers (${dpr.skilledLaborCount + dpr.unskilledLaborCount} Total)
⛅ *Weather:* ${dpr.weatherCondition}

✅ *Work Completed Today:*
${dpr.tasksCompleted}

${dpr.delaysOrIssues ? `⚠️ *Delays / Issues:* \n${dpr.delaysOrIssues}\n` : ''}
${dpr.safetyObservations ? `🦺 *Safety Observations:* \n${dpr.safetyObservations}\n` : ''}
👷‍♂️ *Reported By:* ${dpr.reportedBy}

_Sent via NirmaanSetu Construction Management System_`;

  const encoded = encodeURIComponent(text);
  const target = phoneNumber ? `https://wa.me/${phoneNumber.replace(/[^0-9]/g, '')}?text=${encoded}` : `https://api.whatsapp.com/send?text=${encoded}`;
  window.open(target, '_blank');
};

/**
 * Format and trigger WhatsApp share for an Invoice Payment Reminder
 */
export const shareInvoiceViaWhatsApp = (payment: PaymentRecord, clientPhone?: string): void => {
  const text = `🧾 *NIRMAAN SETU — INVOICE ALERT*
Invoice No: *${payment.invoiceNumber}*
Project: *${payment.projectTitle}*
Amount Due: *₹${payment.totalAmount.toLocaleString('en-IN')}*
Due Date: *${payment.dueDate}*
Status: *${payment.status}*

Please process the invoice remittance at the earliest. For queries, contact accounts@nirmaansetu.in.`;

  const encoded = encodeURIComponent(text);
  const target = clientPhone ? `https://wa.me/${clientPhone.replace(/[^0-9]/g, '')}?text=${encoded}` : `https://api.whatsapp.com/send?text=${encoded}`;
  window.open(target, '_blank');
};

/**
 * Format and trigger WhatsApp share for Attendance Check-In Confirmation
 */
export const shareAttendanceViaWhatsApp = (att: AttendanceRecord, supervisorPhone?: string): void => {
  const text = `📍 *ATTENDANCE CHECK-IN CONFIRMATION*
Worker: *${att.workerName}* (${att.workerTrade})
Site: *${att.siteName}*
Date: *${att.date}* | Time: *${att.checkInTime}*
Geofence Status: *${att.isWithinGeofence ? '✅ Inside Site Boundary' : '⚠️ Outside Site Geofence'}* (${att.distanceFromSiteMeters}m from center)
Approval Status: *${att.approvalStatus}*`;

  const encoded = encodeURIComponent(text);
  const target = supervisorPhone ? `https://wa.me/${supervisorPhone.replace(/[^0-9]/g, '')}?text=${encoded}` : `https://api.whatsapp.com/send?text=${encoded}`;
  window.open(target, '_blank');
};
