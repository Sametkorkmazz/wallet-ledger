import { type TransactionStatus } from "@/api/types";
import { Badge } from "@/components/ui/badge";
import { Check, CircleAlert, Clock } from "lucide-react";
import type { ReactNode } from "react";

const TRANSACTION_STATUS_CLASS: Record<
  TransactionStatus,
  { label: string; icon: ReactNode; className: string }
> = {
  COMPLETED: {
    label: "Completed",
    icon: <Check />,
    className: "text-success bg-success/12 border-success/25",
  },
  PENDING: {
    label: "Pending",
    icon: <Clock />,
    className: "text-warning bg-warning/12 border-warning/25",
  },
  FAILED: {
    label: "Failed",
    icon: <CircleAlert />,
    className: "text-danger bg-danger/12 border-danger/25",
  },
};

export default function StatusBadge({ status }: { status: TransactionStatus }) {
  const statusObj = TRANSACTION_STATUS_CLASS[status];
  return (
    <Badge variant="outline" className={statusObj.className}>
      {statusObj.icon}
      {statusObj.label}
    </Badge>
  );
}
