import { apiClient } from './api-client';

export interface GeocodingSearchResult {
  refId: string;
  display: string;
  address: string;
  name: string;
  lat: number;
  lng: number;
}

export const getAutocomplete = async (text: string, focus?: string): Promise<GeocodingSearchResult[]> => {
  const response = await apiClient.get<GeocodingSearchResult[]>('/geocoding/autocomplete', {
    params: { text, focus },
  });
  return response.data;
};

export const getSearch = async (text: string, focus?: string): Promise<GeocodingSearchResult[]> => {
  const response = await apiClient.get<GeocodingSearchResult[]>('/geocoding/search', {
    params: { text, focus },
  });
  return response.data;
};
