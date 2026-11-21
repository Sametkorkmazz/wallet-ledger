import PageHeader from "@/components/layout/PageHeader";
import { useParams } from "react-router-dom";

export default function TransactionDetailPage() {
  const { id } = useParams();
  return <PageHeader title="Transaction" description={id} />;
}
