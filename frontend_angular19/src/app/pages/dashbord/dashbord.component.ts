import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

interface StatCard {
  title: string;
  value: string | number;
  change: number;
  icon: string;
  color: string;
}

interface ChartPoint {
  x: number;
  y: number;
}

interface RecentActivity {
  id: number;
  user: string;
  action: string;
  timestamp: Date;
  status: 'success' | 'warning' | 'error';
}

@Component({
  selector: 'app-dashbord',
  imports: [CommonModule],
  templateUrl: './dashbord.component.html',
  styleUrls: ['./dashbord.component.css']
})
export class DashbordComponent implements OnInit {

  Math = Math;

  statsCards: StatCard[] = [
    {
      title: 'Total Revenue',
      value: '$45,320',
      change: 12.5,
      icon: 'fas fa-dollar-sign',
      color: 'blue'
    },
    {
      title: 'New Users',
      value: '2,431',
      change: 8.2,
      icon: 'fas fa-users',
      color: 'green'
    },
    {
      title: 'Orders',
      value: '1,892',
      change: -3.1,
      icon: 'fas fa-shopping-cart',
      color: 'orange'
    },
    {
      title: 'Conversion Rate',
      value: '3.2%',
      change: 5.4,
      icon: 'fas fa-chart-line',
      color: 'purple'
    }
  ];

  chartPoints: ChartPoint[] = [
    { x: 0, y: 180 },
    { x: 50, y: 160 },
    { x: 100, y: 140 },
    { x: 150, y: 120 },
    { x: 200, y: 100 },
    { x: 250, y: 80 },
    { x: 300, y: 90 },
    { x: 350, y: 70 },
    { x: 400, y: 60 }
  ];

  recentActivities: RecentActivity[] = [
    {
      id: 1,
      user: 'John Doe',
      action: 'created a new project',
      timestamp: new Date(Date.now() - 300000),
      status: 'success'
    },
    {
      id: 2,
      user: 'Sarah Smith',
      action: 'updated user profile',
      timestamp: new Date(Date.now() - 600000),
      status: 'success'
    },
    {
      id: 3,
      user: 'Mike Johnson',
      action: 'failed to process payment',
      timestamp: new Date(Date.now() - 900000),
      status: 'error'
    },
    {
      id: 4,
      user: 'Emma Wilson',
      action: 'uploaded new document',
      timestamp: new Date(Date.now() - 1200000),
      status: 'warning'
    }
  ];

  ngOnInit(): void {
    // Initialize component
  }

  formatTime(timestamp: Date): string {
    const now = new Date();
    const diff = now.getTime() - timestamp.getTime();
    const minutes = Math.floor(diff / 60000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
    return `${Math.floor(minutes / 1440)}d ago`;
  }

  createUser(): void {
    console.log('Creating new user...');
  }

  generateReport(): void {
    console.log('Generating report...');
  }

  exportData(): void {
    console.log('Exporting data...');
  }

  openSettings(): void {
    console.log('Opening settings...');
  }
}
