import { jsPDF } from 'jspdf';
import { PaymentRecord, WorkerSalaryRecord } from '../types/schema';

/**
 * Generates and downloads a clean, professional GST Tax Invoice PDF
 */
export const generateInvoicePDF = (payment: PaymentRecord, clientDetails?: { address?: string; gstin?: string }): void => {
  const doc = new jsPDF();

  // Header / Branding
  doc.setFillColor(15, 32, 82); // Primary Navy
  doc.rect(0, 0, 210, 35, 'F');

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(22);
  doc.setTextColor(255, 255, 255);
  doc.text('NIRMAAN SETU', 14, 18);

  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(220, 220, 235);
  doc.text('Construction Workforce & Project Management Platform', 14, 25);
  doc.text('GSTIN: 27AABCN8912P1ZV | Email: accounts@nirmaansetu.in', 14, 30);

  // Invoice Title
  doc.setTextColor(15, 32, 82);
  doc.setFontSize(16);
  doc.setFont('helvetica', 'bold');
  doc.text('TAX INVOICE', 140, 50);

  // Invoice Meta
  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(60, 60, 60);
  doc.text(`Invoice No: ${payment.invoiceNumber}`, 140, 58);
  doc.text(`Date: ${payment.createdAt.split('T')[0] || payment.createdAt}`, 140, 64);
  doc.text(`Due Date: ${payment.dueDate || 'Immediate'}`, 140, 70);
  doc.text(`Status: ${payment.status}`, 140, 76);

  // Billed To
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(11);
  doc.setTextColor(15, 32, 82);
  doc.text('BILLED TO:', 14, 50);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  doc.setTextColor(50, 50, 50);
  doc.text(payment.clientName || 'Valued Client', 14, 58);
  doc.text(`Project: ${payment.projectTitle}`, 14, 64);
  if (clientDetails?.address) {
    doc.text(clientDetails.address, 14, 70);
  }
  if (clientDetails?.gstin) {
    doc.text(`Client GSTIN: ${clientDetails.gstin}`, 14, 76);
  }

  // Divider
  doc.setDrawColor(220, 220, 220);
  doc.line(14, 85, 196, 85);

  // Table Header
  doc.setFillColor(245, 245, 250);
  doc.rect(14, 90, 182, 10, 'F');
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(9);
  doc.setTextColor(30, 30, 30);
  doc.text('ITEM / DESCRIPTION', 18, 96);
  doc.text('TYPE', 105, 96);
  doc.text('QTY', 135, 96);
  doc.text('AMOUNT (INR)', 165, 96);

  // Table Row
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  doc.text(payment.description || 'Milestone Construction Services & Workforce Fulfillment', 18, 108, { maxWidth: 80 });
  doc.text(payment.paymentType.replace('_', ' '), 105, 108);
  doc.text('1.0', 135, 108);
  doc.text(`Rs. ${payment.amount.toLocaleString('en-IN')}`, 165, 108);

  // Table Line
  doc.line(14, 125, 196, 125);

  // Financial Breakdown
  const subtotal = payment.amount;
  const gstRate = 0.18;
  const gstAmount = payment.taxAmount !== undefined ? payment.taxAmount : Math.round(subtotal * gstRate);
  const total = subtotal + gstAmount;

  doc.text('Subtotal:', 135, 135);
  doc.text(`Rs. ${subtotal.toLocaleString('en-IN')}`, 165, 135);

  doc.text('GST (18%):', 135, 142);
  doc.text(`Rs. ${gstAmount.toLocaleString('en-IN')}`, 165, 142);

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(11);
  doc.setTextColor(15, 32, 82);
  doc.text('Total Payable:', 135, 152);
  doc.text(`Rs. ${total.toLocaleString('en-IN')}`, 165, 152);

  // Payment Terms & Bank Details
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(10);
  doc.setTextColor(15, 32, 82);
  doc.text('PAYMENT DETAILS & TERMS', 14, 175);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(9);
  doc.setTextColor(70, 70, 70);
  doc.text('Bank: HDFC Bank Ltd | A/C: 50200088921890 | IFSC: HDFC0000123', 14, 182);
  doc.text('UPI ID: nirmaansetu@hdfcbank', 14, 188);
  doc.text('Terms: Payment is due within 7 days of invoice date. Interest @ 1.5% applies for late dues.', 14, 194);

  // Authorized Signatory
  doc.line(140, 220, 196, 220);
  doc.setFont('helvetica', 'italic');
  doc.setFontSize(9);
  doc.text('Authorized Signatory', 150, 226);
  doc.text('NirmaanSetu Technologies Pvt Ltd', 142, 231);

  // Save PDF
  doc.save(`Invoice_${payment.invoiceNumber}.pdf`);
};

/**
 * Generates and downloads a Worker Salary Pay Slip PDF
 */
export const generateSalarySlipPDF = (salary: WorkerSalaryRecord): void => {
  const doc = new jsPDF();

  // Header Banner
  doc.setFillColor(15, 32, 82);
  doc.rect(0, 0, 210, 30, 'F');

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(18);
  doc.setTextColor(255, 255, 255);
  doc.text('NIRMAAN SETU', 14, 16);

  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');
  doc.text('Worker Salary & Wage Disbursement Voucher', 14, 23);

  // Title
  doc.setTextColor(15, 32, 82);
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text(`PAY SLIP FOR MONTH: ${salary.month}`, 14, 45);

  // Worker Info Block
  doc.setFillColor(248, 248, 252);
  doc.roundedRect(14, 52, 182, 30, 3, 3, 'F');

  doc.setFontSize(10);
  doc.setTextColor(50, 50, 50);
  doc.text(`Worker Name: ${salary.workerName}`, 20, 62);
  doc.text(`Worker ID: ${salary.workerId.slice(0, 12)}`, 20, 70);
  doc.text(`Daily Wage Rate: Rs. ${salary.dailyRate}/day`, 110, 62);
  doc.text(`Status: ${salary.status}`, 110, 70);

  // Attendance & Wage Breakdown Table
  doc.setFillColor(235, 238, 245);
  doc.rect(14, 90, 182, 10, 'F');
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 32, 82);
  doc.text('EARNINGS & ATTENDANCE PARTICULARS', 18, 97);
  doc.text('AMOUNT / METRIC', 135, 97);

  doc.setFont('helvetica', 'normal');
  doc.setTextColor(40, 40, 40);
  doc.text('Full Days Present', 18, 110);
  doc.text(`${salary.daysPresent} Days`, 140, 110);

  doc.text('Half Days Worked', 18, 118);
  doc.text(`${salary.halfDays} Days`, 140, 118);

  doc.text('Overtime Hours', 18, 126);
  doc.text(`${salary.overtimeHours} Hours`, 140, 126);

  doc.text('Base Wage (Days × Daily Rate)', 18, 134);
  doc.text(`Rs. ${salary.baseSalary.toLocaleString('en-IN')}`, 140, 134);

  doc.text('Overtime Earnings', 18, 142);
  doc.text(`Rs. ${salary.overtimePay.toLocaleString('en-IN')}`, 140, 142);

  doc.text('Performance Bonus / Allowances', 18, 150);
  doc.text(`Rs. ${salary.bonus.toLocaleString('en-IN')}`, 140, 150);

  doc.text('Deductions / Advances Settled', 18, 158);
  doc.text(`- Rs. ${salary.deductions.toLocaleString('en-IN')}`, 140, 158);

  // Net Pay Highlight
  doc.setFillColor(240, 248, 240);
  doc.rect(14, 168, 182, 14, 'F');
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(12);
  doc.setTextColor(22, 101, 52);
  doc.text('NET SALARY PAYABLE:', 18, 177);
  doc.text(`Rs. ${salary.netPayable.toLocaleString('en-IN')}`, 140, 177);

  // Footer notes
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(9);
  doc.setTextColor(100, 100, 100);
  doc.text(`Payment Mode: ${salary.paymentMethod || 'Direct Bank/UPI'} | Ref: ${salary.paymentRef || 'N/A'}`, 14, 195);
  doc.text('This is a computer-generated voucher issued by NirmaanSetu Workforce Platform.', 14, 202);

  // Signatures
  doc.line(20, 235, 75, 235);
  doc.text('Worker Signature / Thumb Impression', 20, 242);

  doc.line(135, 235, 190, 235);
  doc.text('Site Supervisor Signature', 140, 242);

  doc.save(`Salary_Slip_${salary.workerName.replace(/\s+/g, '_')}_${salary.month}.pdf`);
};
