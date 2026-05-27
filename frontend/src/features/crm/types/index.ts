export type LeadType = 'LEAD' | 'OPPORTUNITY';

export interface CrmStage {
  id: string;
  name: string;
  sequence: number;
  isFolded: boolean;
  probability: number;
}

export interface UserBase {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
}

export interface PartnerBase {
  id: string;
  name: string;
  email: string;
  phone: string;
}

export interface SalesTeam {
  id: string;
  name: string;
}

export interface CrmLead {
  id: string;
  name: string;
  type: LeadType;
  expectedRevenue: number;
  probability: number;
  stage: CrmStage;
  partner?: PartnerBase;
  salesperson?: UserBase;
  salesTeam?: SalesTeam;
  createdAt?: string;
  updatedAt?: string;
}

export interface CrmAppointment {
  id: string;
  title: string;
  startTime: string;
  endTime: string;
  location?: string;
  description?: string;
  status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
  lead?: CrmLead;
}

export interface CreateCrmLeadRequest {
  name: string;
  type: LeadType;
  expectedRevenue?: number;
  probability?: number;
  stageId?: string;
  partnerId?: string;
  salespersonId?: string;
  salesTeamId?: string;
}
