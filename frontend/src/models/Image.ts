
export function validateImageFile(file: File) {
  if (!file) {
    return { isValid:false, errorMessage:'No file selected', file : null };
  }

  const fileName = file.name.toLowerCase();
  const validExtensions = ['.jpg', '.jpeg', '.png'];
  const validTypes = ['image/jpeg', 'image/png'];
  const hasValidExtension = validExtensions.some(ext => fileName.endsWith(ext));
  const hasValidType = validTypes.includes(file.type);
  if (!hasValidExtension || !hasValidType) {
    return { isValid: false, errorMessage: 'Only PNG and JPG files are allowed', file: null };
  }
  return { isValid: true, errorMessage: '', file: file };
}

export function createImagePreview(file: File): Promise<string> {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      if (e.target?.result) {
        resolve(e.target.result as string);
      }
    };
    reader.readAsDataURL(file);
  });
}

export async function fetchImageWithAuth(imageUrl: string, authHeaders: HeadersInit) {
  const response = await fetch(imageUrl, { headers: authHeaders });
  if (response.ok) {
    const blob = await response.blob();
    return URL.createObjectURL(blob);
  }
  return null;
}

export function getBase64ImageUrl(base64Data: string) {
  return `data:image/png;base64,${base64Data}`;
}

export function cleanupImageUrls(imageUrls: Record<string, string>) {
  for (const key in imageUrls) {
    const url = imageUrls[key];
    URL.revokeObjectURL(url);
  }
}
