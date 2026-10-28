import React, { useRef, useState, useEffect } from 'react';
import { Camera as CameraIcon, RefreshCw, Check, Upload, AlertCircle, Smartphone } from 'lucide-react';
import { Capacitor } from '@capacitor/core';
import { Camera as CapCamera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Modal } from './Modal';

interface CameraCaptureModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCapture: (imageDataUrl: string) => void;
  title?: string;
}

export const CameraCaptureModal: React.FC<CameraCaptureModalProps> = ({
  isOpen,
  onClose,
  onCapture,
  title = 'Selfie Attendance Verification',
}) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [capturedImage, setCapturedImage] = useState<string | null>(null);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [loadingCamera, setLoadingCamera] = useState(false);
  const isNative = Capacitor.isNativePlatform();

  const handleNativeCamera = async () => {
    setCameraError(null);
    setLoadingCamera(true);
    try {
      const image = await CapCamera.getPhoto({
        quality: 85,
        allowEditing: false,
        resultType: CameraResultType.DataUrl,
        source: CameraSource.Camera,
      });

      if (image.dataUrl) {
        setCapturedImage(image.dataUrl);
      }
    } catch (err: any) {
      if (!err.message?.includes('cancelled') && !err.message?.includes('canceled')) {
        setCameraError(err.message || 'Failed to capture photo from native camera.');
      }
    } finally {
      setLoadingCamera(false);
    }
  };

  const startWebCamera = async () => {
    setCameraError(null);
    setLoadingCamera(true);
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Camera access is not supported by your browser.');
      }
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
        audio: false,
      });
      setStream(mediaStream);
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
    } catch (err: any) {
      console.warn('Camera stream error:', err);
      setCameraError('Unable to access camera directly. You can upload a photo instead.');
    } finally {
      setLoadingCamera(false);
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      setStream(null);
    }
  };

  useEffect(() => {
    if (isOpen) {
      setCapturedImage(null);
      if (isNative) {
        // Automatically launch native camera on mobile device
        handleNativeCamera();
      } else {
        startWebCamera();
      }
    } else {
      stopCamera();
    }
    return () => {
      stopCamera();
    };
  }, [isOpen]);

  const handleSnap = () => {
    if (videoRef.current && canvasRef.current) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth || 640;
      canvas.height = video.videoHeight || 480;
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
        setCapturedImage(dataUrl);
        stopCamera();
      }
    }
  };

  const handleRetake = () => {
    setCapturedImage(null);
    if (isNative) {
      handleNativeCamera();
    } else {
      startWebCamera();
    }
  };

  const handleConfirm = () => {
    if (capturedImage) {
      onCapture(capturedImage);
      onClose();
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setCapturedImage(reader.result as string);
        stopCamera();
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
        {capturedImage ? (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', width: '100%' }}>
            <div
              style={{
                width: '260px',
                height: '260px',
                borderRadius: '50%',
                overflow: 'hidden',
                border: '4px solid var(--primary-color)',
                boxShadow: 'var(--shadow-lg)',
              }}
            >
              <img src={capturedImage} alt="Captured Attendance Selfie" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button className="btn btn-secondary" onClick={handleRetake}>
                <RefreshCw size={16} /> Retake
              </button>
              <button className="btn btn-primary" onClick={handleConfirm}>
                <Check size={16} /> Use This Photo
              </button>
            </div>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', width: '100%' }}>
            {cameraError ? (
              <div
                style={{
                  padding: '1.25rem',
                  backgroundColor: 'rgba(239, 68, 68, 0.08)',
                  border: '1px solid var(--error-color)',
                  borderRadius: 'var(--radius-md)',
                  textAlign: 'center',
                  width: '100%',
                  boxSizing: 'border-box',
                }}
              >
                <AlertCircle size={32} color="var(--error-color)" style={{ marginBottom: '0.5rem' }} />
                <p style={{ margin: '0 0 1rem 0', fontSize: '0.875rem', color: 'var(--error-color)' }}>{cameraError}</p>
                <label className="btn btn-primary" style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                  <Upload size={16} /> Upload Photo from Gallery
                  <input type="file" accept="image/*" capture="user" style={{ display: 'none' }} onChange={handleFileUpload} />
                </label>
              </div>
            ) : isNative ? (
              <div style={{ textAlign: 'center', padding: '2rem 1rem' }}>
                <Smartphone size={48} color="var(--primary-color)" style={{ marginBottom: '1rem' }} />
                <h4 style={{ margin: '0 0 0.5rem 0' }}>Native Device Camera</h4>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
                  Click below to open the device camera for verification selfie.
                </p>
                <button className="btn btn-primary" onClick={handleNativeCamera} disabled={loadingCamera}>
                  <CameraIcon size={16} /> Launch Device Camera
                </button>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', width: '100%' }}>
                <div
                  style={{
                    position: 'relative',
                    width: '280px',
                    height: '280px',
                    borderRadius: '50%',
                    overflow: 'hidden',
                    border: '4px solid var(--primary-color)',
                    backgroundColor: '#1E293B',
                  }}
                >
                  <video
                    ref={videoRef}
                    autoPlay
                    playsInline
                    muted
                    style={{ width: '100%', height: '100%', objectFit: 'cover', transform: 'scaleX(-1)' }}
                  />
                  <canvas ref={canvasRef} style={{ display: 'none' }} />
                </div>

                <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                  <button className="btn btn-primary" onClick={handleSnap}>
                    <CameraIcon size={18} /> Capture Selfie
                  </button>
                  <label className="btn btn-secondary" style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                    <Upload size={16} /> Upload
                    <input type="file" accept="image/*" style={{ display: 'none' }} onChange={handleFileUpload} />
                  </label>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </Modal>
  );
};
