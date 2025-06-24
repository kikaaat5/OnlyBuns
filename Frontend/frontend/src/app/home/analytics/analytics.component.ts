import { Component, OnInit } from '@angular/core';
import { AnalyticsService } from '../../service/analytics.service'; 
import { PostCommentStats, ClientActivityStats } from '../../model/analytics.model'; 
import { LegendPosition } from '@swimlane/ngx-charts';
import { ActivityCategory } from '../../model/analytics.model';
import { LegendOptions } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent implements OnInit {

  postCommentStats: PostCommentStats | null = null;
  clientActivityStats: ClientActivityStats | null = null;
  clientActivityChartData: ActivityCategory[] = [];
  view: [number, number] = [700, 350]; // Dimenzije grafika [širina, visina]
  gradient: boolean = true;
  showLabels: boolean = false;
  colorScheme: string = 'cool';
  isLoading: boolean = true; 
  errorMessage: string | null = null; 
  legendPosition: LegendPosition = LegendPosition.Below;
  tooltipDisabled: boolean = false;

  constructor(private analyticsService: AnalyticsService) { } 

  ngOnInit(): void {
    this.fetchAnalyticsData();
  }

  customTooltipText(args: { data: { name: string; value: number; percent?: number } }): string {
    let displayPercentValue: number;

    if (args.data.percent !== undefined && typeof args.data.percent === 'number' && !isNaN(args.data.percent)) {
      displayPercentValue = args.data.percent;
    } else {
      displayPercentValue = parseFloat(args.data.value.toString());
    }

    let formattedPercent = 'N/A'; 
    if (typeof displayPercentValue === 'number' && !isNaN(displayPercentValue)) {
      formattedPercent = displayPercentValue.toFixed(2); 
    }
    
    return `
      <span class="tooltip-name">${args.data.name}</span><br>
      <span class="tooltip-percent">${formattedPercent}%</span>
    `;
  }

  fetchAnalyticsData(): void {
    this.isLoading = true; 
    this.errorMessage = null; 

    this.analyticsService.getPostCommentStats().subscribe({
      next: (data) => {
        this.postCommentStats = data;
      },
      error: (err) => {
        console.error('Greška pri dohvaćanju statistike objava i komentara:', err);
        this.errorMessage = 'Nije moguće učitati statistiku objava i komentara.';
        this.isLoading = false; 
      }
    });

    this.analyticsService.getClientActivityStats().subscribe({
      next: (data) => {
        this.clientActivityStats = data;
        this.isLoading = false; 
        console.log('Podaci o statistici aktivnosti klijenata (clientActivityStats):', data);
      },
      error: (err) => {
        console.error('Greška pri dohvaćanju statistike aktivnosti klijenata:', err);
        this.errorMessage = this.errorMessage ? this.errorMessage + '\nNije moguće učitati statistiku aktivnosti klijenata.' : 'Nije moguće učitati statistiku aktivnosti klijenata.';
        this.isLoading = false; 
      }
    });
  }

  formatLabels(data: ActivityCategory[]): ActivityCategory[] {
    return data.map(item => {
      let newName = item.name;
      if (item.name === 'Users with posts') {
        newName = 'Posts';
      } else if (item.name === 'Users with only comments') {
        newName = 'Comments';
      } else if (item.name === 'Inactive users') {
        newName = 'Inactive';
      }
      
      return {
        name: newName,
        value: item.value
      };
    });
  }

  onSelect(data: any): void {
    console.log('Stavka selektovana:', data);
  }
}