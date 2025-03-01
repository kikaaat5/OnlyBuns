import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private apiUrl = 'http://localhost:8080/bunny-care/locations';

  constructor(private http: HttpClient) {}

  getLocations(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
}
